(ns ko-premium-algo.upbit.transfer
  (:refer-clojure :exclude [methods])
  (:require [clj-http.client :as client]
            [ko-premium-algo.upbit.auth :as auth]
            [ko-premium-algo.wallet.terms :refer [make-terms]]
            [ko-premium-algo.wallet.limits :refer [make-limits]]
            [ko-premium-algo.lib.range :refer [make-range]]
            [ko-premium-algo.trade.fee :refer [make-fee]]
            [ko-premium-algo.wallet.intent :refer [address unit qty make-intent]]
            [ko-premium-algo.wallet.transfer :refer [make-transfer]]
            [ko-premium-algo.wallet.unit :refer [make-unit asset method]]
            [ko-premium-algo.lib.time :refer [iso8601->time make-duration millis]]
            [cheshire.core :as json]
            [clojure.core.async :refer [go-loop <! timeout go]]
            [ko-premium-algo.lib.file :refer [make-file-manager]]
            [ko-premium-algo.lib.async :refer [sequential]]))

(defn- response->transfer [response]
  (make-transfer (get response "uuid")
                 (get response "txid")
                 (keyword (get response "type"))
                 (make-intent "unknown"
                              (make-unit (get response "currency")
                                         (get response "net_type"))
                              (get response "amount"))
                 (iso8601->time (get response "created_at"))
                 (get response "state")))

(defn execute-withdraw [intent]
  (let [request {:currency (asset (unit intent))
                 :net_type (method (unit intent))
                 :amount (qty intent)
                 :address (address intent)}]
    (->> (client/post "https://api.upbit.com/v1/withdraws/coin"
                      {:headers (auth/make-auth-header request)
                       :body request})
         (#(json/parse-string (:body %)))
         (#(response->transfer %)))))

(defn transfer [side txid]
  (->> (client/get (if (= side :deposit)
                     "https://api.upbit.com/v1/deposit"
                     "https://api.upbit.com/v1/withdraw")
                   {:headers (auth/make-auth-header {:txid txid})
                    :query-params {:txid txid}})
       (#(json/parse-string (:body %)))
       (#(response->transfer %))))

(def ^:private manager
  (make-file-manager ".cache/upbit.transfer.edn"))

(defn- unit->key [unit]
  (str (asset unit) "-" (method unit)))

(defn units []
  (->> (client/get "https://api.upbit.com/v1/status/wallet"
                   {:headers (auth/make-auth-header)})
       (#(json/parse-string (:body %)))
       (map #(make-unit (get % "currency") (get % "net_type")))))

(defn terms [units]
  (let [cache (or (manager :read) {})]
    (map #(get cache %) (map unit->key units))))

(defn- base-terms [unit]
  (->> (client/get "https://api.upbit.com/v1/withdraws/chance"
                   {:headers (auth/make-auth-header {:currency (asset unit) :net_type (method unit)})
                    :query-params {:currency (asset unit) :net_type (method unit)}})
       (#(json/parse-string (:body %)))
       (#(make-terms (make-fee :fixed (Float/parseFloat (get-in % ["currency" "withdraw_fee"])))
                     (make-limits (make-range (Float/parseFloat (get-in % ["withdraw_limit" "minimum"]))
                                              Float/POSITIVE_INFINITY
                                              (Math/pow 0.1 (get-in % ["withdraw_limit" "fixed"])))
                                  (set (map keyword (get-in % ["currency" "wallet_support"]))))))))

(defn terms-map [units terms-list]
  (->> (map #(vector (unit->key %1) %2) units terms-list)
       (into {})))

(defn batch []
  (let [units (units)]
    (go (->> units
             (map #(fn [] (base-terms %)))
             (apply sequential)
             <!
             (terms-map units)
             (manager :save)))))

(go-loop []
  (<! (batch))
  (<! (timeout (millis (make-duration 1 "h"))))
  (recur))
