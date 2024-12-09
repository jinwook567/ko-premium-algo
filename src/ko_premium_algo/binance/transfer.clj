(ns ko-premium-algo.binance.transfer
  (:refer-clojure :exclude [methods])
  (:require [clj-http.client :as client]
            [ko-premium-algo.binance.auth :as auth]
            [ko-premium-algo.wallet.terms :refer [make-terms]]
            [ko-premium-algo.wallet.limits :refer [make-limits]]
            [ko-premium-algo.lib.range :refer [make-range]]
            [ko-premium-algo.trade.fee :refer [make-fee]]
            [ko-premium-algo.wallet.intent :refer [address asset method qty make-intent]]
            [ko-premium-algo.wallet.transfer :refer [make-transfer]]
            [ko-premium-algo.lib.time :refer [millis->time]]
            [cheshire.core :as json]))

(defn- true-keys [map]
  (into #{} (for [[k v] map :when v] k)))

(defn network->terms [network]
  (merge {:method (get network "network")}
         (make-terms (make-fee :fixed (Float/parseFloat (get network "withdrawFee")))
                     (make-limits (make-range (Float/parseFloat (get network "withdrawMin"))
                                              (Float/parseFloat (get network "withdrawMax"))
                                              (Float/parseFloat (get network "withdrawIntegerMultiple")))
                                  (true-keys {:deposit (get network "depositEnable")
                                              :withdraw (get network "withdrawEnable")})))))

(defn- terms-list []
  (->> (client/get "https://api.binance.com/sapi/v1/capital/config/getall"
                   {:headers (auth/make-auth-header)
                    :query-params (auth/make-payload)})
       (#(json/parse-string (:body %)))
       (mapcat #(map (fn [network] (merge (network->terms network)
                                          {:asset (get % "coin")}))
                     (get % "networkList")))))

(defn methods [asset]
  (filter #(= (:asset %) asset) (terms-list)))

(defn terms [asset method]
  (some #(when (and (= (:asset %) asset)
                    (= (:method %) method)) %) (terms-list)))

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
                                    (get % "network")
                                    (get % "coin")
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




