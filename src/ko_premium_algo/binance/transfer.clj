(ns ko-premium-algo.binance.transfer
  (:require [clj-http.client :as client]
            [ko-premium-algo.binance.auth :as auth]
            [ko-premium-algo.wallet.terms :refer [make-terms limits]]
            [ko-premium-algo.wallet.limits :refer [make-limits actions]]
            [ko-premium-algo.lib.range :refer [make-range]]
            [ko-premium-algo.trade.fee :refer [make-fee]]
            [ko-premium-algo.wallet.intent :refer [address qty make-intent unit]]
            [ko-premium-algo.wallet.unit :refer [make-unit asset method]]
            [ko-premium-algo.wallet.transfer :refer [make-transfer]]
            [ko-premium-algo.wallet.address :refer [make-address primary-address secondary-address]]
            [ko-premium-algo.lib.time :refer [millis->time]]
            [ko-premium-algo.lib.numeric :refer [str->num]]
            [cheshire.core :as json]
            [clojure.string :as string]
            [clojure.set :refer [intersection]]
            [ko-premium-algo.lib.file :refer [make-file-manager]]))

(defn- true-keys [map]
  (into #{} (for [[k v] map :when v] k)))

(defn- unit->key [unit]
  (str (asset unit) "-" (method unit)))

(defn- key->unit [unit-key]
  (let [parts (string/split unit-key #"-")]
    (make-unit (first parts) (second parts))))

(defn- network->terms [network]
  (make-terms (make-fee :fixed (str->num (get network "withdrawFee")))
              (make-limits (make-range (str->num (get network "withdrawMin"))
                                       (str->num (get network "withdrawMax"))
                                       (when (= (str->num (get network "withdrawIntegerMultiple")) 0)
                                         (str->num (get network "withdrawIntegerMultiple"))))
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
                       (network->terms %2)) {})))

(defn units []
  (map key->unit (keys (terms-info))))

(defn terms [units]
  (let [info (terms-info)]
    (map #(get info (unit->key %)) units)))

(defn- deposit-status [code]
  (cond
    (= code 1) :done
    (contains? #{2 6 7} code) :error
    :else :open))

(defn- withdraw-status [code]
  (cond
    (= code 6) :done
    (= code 3) :error
    :else :open))

(defn- status [code withdraw?]
  (if withdraw? (withdraw-status code) (deposit-status code)))

(defn transfer [side id-type id]
  (let [response-property (case id-type
                            :id "id"
                            :txid "txId")]
    (->> (client/get (if (= side :deposit)
                       "https://api.binance.com/sapi/v1/capital/deposit/hisrec"
                       "https://api.binance.com/sapi/v1/capital/withdraw/history")
                     {:headers (auth/make-auth-header)
                      :query-params (auth/make-payload)})
         (#(json/parse-string (:body %)))
         (some #(when (= (get % response-property) id) %))
         (#(make-transfer (get % "id")
                          (get % "txId")
                          side
                          (make-intent (get % "address")
                                       (make-unit (get % "coin")
                                                  (get % "network"))
                                       (str->num (get % "amount")))
                          (millis->time (get % "insertTime"))
                          (status (get % "status") (= side :withdraw)))))))

(defn execute-withdraw [intent]
  (->> (client/post "https://api.binance.com/sapi/v1/capital/withdraw/apply"
                    {:headers (auth/make-auth-header)
                     :query-params (auth/make-payload (merge {:coin (asset (unit intent))
                                                              :network (method (unit intent))
                                                              :amount (qty intent)
                                                              :address (primary-address (address intent))}
                                                             (when-let [sa (secondary-address (address intent))] {:addressTag sa})))})
       (#(json/parse-string (:body %)))
       (#(transfer :withdraw :id (get % "id")))))

(defn deposit-address [unit]
  (->> (client/get "https://api.binance.com/sapi/v1/capital/deposit/address"
                   {:headers (auth/make-auth-header)
                    :query-params (auth/make-payload {:coin (asset unit)
                                                      :network (method unit)})})
       (#(json/parse-string (:body %)))
       (#(make-address (get % "address") (if (= (get % "tag") "") nil (get % "tag"))))))

(defn make-file-for-register-address [other-exchange-units file-name]
  (let [support-units (intersection (set other-exchange-units) (set (units)))
        terms-list (terms support-units)]
    (->> (map vector support-units terms-list)
         (filter #(contains? (actions (limits (second %))) :deposit))
         (map first)
         (map (fn [unit] {:asset (asset unit) :network (method unit) :address (deposit-address unit)}))
         (group-by (fn [info] [(:address info) (:network info)]))
         (map #(vector (first %) (map :asset (second %))))
         (into {})
         ((make-file-manager file-name) :save))))
