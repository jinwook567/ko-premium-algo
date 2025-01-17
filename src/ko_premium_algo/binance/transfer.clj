(ns ko-premium-algo.binance.transfer
  (:require [clj-http.client :as client]
            [ko-premium-algo.binance.auth :as auth]
            [ko-premium-algo.wallet.terms :refer [make-terms]]
            [ko-premium-algo.wallet.limits :refer [make-limits]]
            [ko-premium-algo.lib.range :refer [make-range]]
            [ko-premium-algo.trade.fee :refer [make-fee]]
            [ko-premium-algo.wallet.intent :refer [address qty make-intent]]
            [ko-premium-algo.wallet.unit :refer [make-unit asset method]]
            [ko-premium-algo.wallet.transfer :refer [make-transfer]]
            [ko-premium-algo.lib.time :refer [millis->time]]
            [cheshire.core :as json]
            [clojure.string :as string]))

(defn- true-keys [map]
  (into #{} (for [[k v] map :when v] k)))

(defn- unit->key [unit]
  (str (asset unit) "-" (method unit)))

(defn- key->unit [unit-key]
  (let [parts (string/split unit-key #"-")]
    (make-unit (first parts) (second parts))))

(defn- network->terms [network]
  (make-terms (make-fee :fixed (Float/parseFloat (get network "withdrawFee")))
              (make-limits (make-range (Float/parseFloat (get network "withdrawMin"))
                                       (Float/parseFloat (get network "withdrawMax"))
                                       (Float/parseFloat (get network "withdrawIntegerMultiple")))
                           (true-keys {:deposit (get network "depositEnable")
                                       :withdraw (get network "withdrawEnable")}))))

(defn- terms-info []
  (->> (client/get "https://api.binance.com/sapi/v1/capital/config/getall"
                   {:headers (auth/make-auth-header)
                    :query-params (auth/make-payload)})
       (#(json/parse-string (:body %)))
       (mapcat #(get % "networkList"))
       (reduce #(assoc %1
                       (unit->key (make-unit (get %2 "coin") (get %2 "network")))
                       (network->terms %2)))))

(defn units []
  (map key->unit (keys (terms-info))))

(defn terms [units]
  (let [info (terms-info)]
    (map #(get info (unit->key %)) units)))

(defn- status [code]
  (cond
    (= code 2) "WAITING"
    (= code 4) "PROCESSING"
    (= code 6) "DONE"
    (= code 5) "FAILED"
    (= code 1) "CANCELLED"
    (= code 3) "REJECTED"))

(defn transfer [side id]
  (->> (client/get (if (= side :deposit)
                     "https://api.binance.com/sapi/v1/capital/deposit/hisrec"
                     "https://api.binance.com/sapi/v1/capital/withdraw/history")
                   {:headers (auth/make-auth-header)
                    :query-params (auth/make-payload)})
       (#(json/parse-string (:body %)))
       (some #(when (= (get % "id") id) %))
       #(make-transfer (get % "id")
                       (get % "txid")
                       side
                       (make-intent (get % "address")
                                    (make-unit (get % "coin")
                                               (get % "network"))
                                    (Float/parseFloat (get % "amount")))
                       (millis->time (get % "insertTime"))
                       (status (get % "status")))))

(defn execute-withdraw [intent]
  (->> (client/post "https://api.binance.com/sapi/v1/capital/withdraw/apply"
                    {:headers (auth/make-auth-header)
                     :query-params (auth/make-payload {:coin (asset intent)
                                                       :network (method intent)
                                                       :amount (qty intent)
                                                       :address (address intent)})})
       (#(json/parse-string (:body %)))
       (#(transfer :withdraw (get % "id")))))




