(ns ko-premium-algo.upbit.terms
  (:require [ko-premium-algo.trade.fee :refer [make-fee]]
            [ko-premium-algo.upbit.auth :as auth]
            [clj-http.client :as client]
            [ko-premium-algo.trade.market :as m]
            [ko-premium-algo.trade.terms :refer [make-market-terms make-terms]]
            [ko-premium-algo.trade.limits :refer [make-limits]]
            [ko-premium-algo.lib.range :refer [make-range]]
            [ko-premium-algo.lib.file :refer [make-file-manager]]
            [ko-premium-algo.upbit.markets :refer [markets]]
            [ko-premium-algo.lib.async :refer [sequential]]
            [clojure.core.async :refer [<! go go-loop timeout]]
            [ko-premium-algo.lib.time :refer [make-duration millis]]
            [cheshire.core :as json]))

(defn- make-side-terms [response side]
  (make-terms (make-fee :rate (Float/parseFloat (get response (str side "_fee"))))
              (make-limits (make-range 0 Float/POSITIVE_INFINITY 0.00000001)
                           (make-range 0 Float/POSITIVE_INFINITY (get-in response ["market" side "price_unit"]))
                           (make-range (Float/parseFloat (get-in response ["market" side "min_total"]))
                                       (Float/parseFloat (get-in response ["market" "max_total"]))
                                       nil))))

(defn base-terms [market]
  (->> (client/get "https://api.upbit.com/v1/orders/chance"
                   {:headers (auth/make-auth-header {:market (m/symbol market)})
                    :query-params {:market (m/symbol market)}})
       (#(json/parse-string (:body %)))
       (#(make-market-terms
          (make-side-terms % "ask")
          (make-side-terms % "bid")))))

(def ^:private manager
  (make-file-manager ".cache/upbit.terms.edn"))

(defn terms [markets]
  (let [cache (or (manager :read) {})]
    (map #(get cache (m/symbol %)) markets)))

(defn- terms-map [markets terms]
  (into {} (map #(vector (m/symbol %1) %2) markets terms)))

(defn batch []
  (let [markets (markets)]
    (go (->> markets
             (map #(fn [] (base-terms %)))
             (apply sequential)
             <!
             (terms-map markets)
             (manager :save)))))

(go-loop []
  (<! (batch))
  (<! (timeout (millis (make-duration 1 "h"))))
  (recur))
