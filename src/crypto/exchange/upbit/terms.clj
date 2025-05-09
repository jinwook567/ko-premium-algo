(ns crypto.exchange.upbit.terms
  (:require [model.fee :refer [make-fee]]
            [crypto.exchange.upbit.auth :as auth]
            [clj-http.client :as client]
            [model.market :as m]
            [model.terms :refer [make-market-terms make-terms]]
            [model.limits :refer [make-limits]]
            [model.range :refer [make-range]]
            [lib.file :refer [make-file-manager]]
            [crypto.exchange.upbit.markets :refer [markets]]
            [lib.async :refer [sequential]]
            [clojure.core.async :refer [<! go go-loop timeout]]
            [mode.time :refer [make-duration millis]]
            [cheshire.core :as json]))

(defn- make-side-terms [response side]
  (make-terms (make-fee :rate :additional (get response (str side "_fee")))
              (make-limits (make-range 0 Float/POSITIVE_INFINITY 0.00000001)
                           (make-range 0 Float/POSITIVE_INFINITY (get-in response ["market" side "price_unit"]))
                           (make-range (get-in response ["market" side "min_total"])
                                       (get-in response ["market" "max_total"])
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
