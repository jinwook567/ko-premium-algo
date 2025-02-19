(ns ko-premium-algo.upbit.transfer
  (:refer-clojure :exclude [methods])
  (:require [clj-http.client :as client]
            [ko-premium-algo.upbit.auth :as auth]
            [ko-premium-algo.wallet.terms :refer [make-terms limits]]
            [ko-premium-algo.wallet.limits :refer [make-limits actions]]
            [ko-premium-algo.lib.range :refer [make-range decimal-step]]
            [ko-premium-algo.trade.fee :refer [make-fee]]
            [ko-premium-algo.wallet.intent :refer [address unit qty make-intent]]
            [ko-premium-algo.wallet.transfer :refer [make-transfer]]
            [ko-premium-algo.wallet.unit :refer [make-unit asset method]]
            [ko-premium-algo.wallet.address :refer [make-address primary-address secondary-address]]
            [ko-premium-algo.lib.time :refer [iso8601->time make-duration millis]]
            [cheshire.core :as json]
            [clojure.core.async :refer [go-loop <! timeout go]]
            [ko-premium-algo.lib.file :refer [make-file-manager]]
            [ko-premium-algo.lib.async :refer [sequential]]))

(defn- withdraw-status [code]
  (cond
    (= code "DONE") :done
    (= code "CANCELLED") :cancelled
    (contains? #{"REJECTED" "FAILED"} code) :error
    :else :open))

(defn- deposit-status [code]
  (cond
    (= code "ACCEPTED") :done
    (= code "REJECTED") :error
    (contains? #{"REFUNDED" "CANCELLED"} code) :cancelled
    :else :open))

(defn- status [code withdraw?]
  (if withdraw? (withdraw-status code) (deposit-status code)))

(defn- response->transfer [response]
  (make-transfer (get response "uuid")
                 (get response "txid")
                 (keyword (get response "type"))
                 (make-intent "unknown"
                              (make-unit (get response "currency")
                                         (get response "net_type"))
                              (get response "amount"))
                 (iso8601->time (get response "created_at"))
                 (status (get response "state") (= :withdraw (keyword (get response "type"))))))

(defn execute-withdraw [intent]
  (let [request (merge {:currency (asset (unit intent))
                        :net_type (method (unit intent))
                        :amount (qty intent)
                        :address (primary-address (address intent))}
                       (when-let [sa (secondary-address (address intent))] {:secondary-address sa}))]
    (->> (client/post "https://api.upbit.com/v1/withdraws/coin"
                      {:headers (auth/make-auth-header (json/decode (json/encode request)))
                       :body (json/encode request)
                       :content-type :json})
         (#(json/parse-string (:body %)))
         (#(response->transfer %)))))

(defn transfer [side id-type id]
  (let [request (case id-type
                  :id {:uuid id}
                  :txid {:txid id})]
    (->> (client/get (if (= side :deposit)
                       "https://api.upbit.com/v1/deposit"
                       "https://api.upbit.com/v1/withdraw")
                     {:headers (auth/make-auth-header request)
                      :query-params request})
         (#(json/parse-string (:body %)))
         (#(response->transfer %)))))

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
       (#(make-terms (make-fee :fixed :inclusive (get-in % ["currency" "withdraw_fee"]))
                     (make-limits (make-range (get-in % ["withdraw_limit" "minimum"])
                                              Float/POSITIVE_INFINITY
                                              (decimal-step (get-in % ["withdraw_limit" "fixed"])))
                                  (set (map keyword (get-in % ["currency" "wallet_support"]))))))))

(defn terms-map [units terms-list]
  (->> (map #(vector (unit->key %1) %2) units terms-list)
       (into {})))

(defn batch-terms []
  (let [units (units)]
    (go (->> units
             (map #(fn [] (base-terms %)))
             (apply sequential)
             <!
             (terms-map units)
             (manager :save)))))

(defn- make-deposit-address [unit]
  (let [request {:currency (asset unit) :net_type (method unit)}]
    (->> (client/post "https://api.upbit.com/v1/deposits/generate_coin_address"
                      {:headers (auth/make-auth-header (json/decode (json/encode request)))
                       :body (json/encode request)
                       :content-type :json}))))

(defn deposit-address [unit]
  (let [request {:currency (asset unit) :net_type (method unit)}]
    (->> (client/get "https://api.upbit.com/v1/deposits/coin_address"
                     {:headers (auth/make-auth-header request)
                      :query-params request})
         (#(json/parse-string (:body %)))
         (#(make-address (get % "deposit_address") (get % "secondary_address"))))))

(defn batch-deposit-address []
  (let [units (units)]
    (go (->> units
             (map (fn [unit]
                    (if (contains? (actions (limits (base-terms unit))) :deposit)
                      (fn [] (go (make-deposit-address unit)
                                 (<! (timeout (millis (make-duration 3 "s"))))))
                      (fn [] "do nothing"))))
             (apply sequential)))))

(go-loop []
  (<! (batch-deposit-address))
  (<! (timeout (millis (make-duration 1 "d"))))
  (recur))

(go-loop []
  (<! (batch-terms))
  (<! (timeout (millis (make-duration 1 "h"))))
  (recur))
