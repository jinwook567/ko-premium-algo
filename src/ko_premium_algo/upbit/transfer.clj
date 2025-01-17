(ns ko-premium-algo.upbit.transfer
  (:refer-clojure :exclude [methods])
  (:require [clj-http.client :as client]
            [ko-premium-algo.upbit.auth :as auth]
            [ko-premium-algo.wallet.terms :refer [make-terms]]
            [ko-premium-algo.wallet.limits :refer [make-limits]]
            [ko-premium-algo.lib.range :refer [make-range]]
            [ko-premium-algo.trade.fee :refer [make-fee]]
            [ko-premium-algo.wallet.intent :refer [address asset method qty make-intent]]
            [ko-premium-algo.wallet.transfer :refer [make-transfer]]
            [ko-premium-algo.lib.time :refer [iso8601->time]]
            [cheshire.core :as json]
            [clojure.core.async :refer [chan go-loop >! <! timeout]]
            [ko-premium-algo.lib.file :refer [make-file-manager]]))

(defn methods [asset]
  (->> (client/get "https://api.upbit.com/v1/status/wallet"
                   {:headers (auth/make-auth-header)})
       (#(json/parse-string (:body %)))
       (filter #(= (get % "currency") asset))
       (map #(get % "net_type"))))

(defn all-methods []
  (->> (client/get "https://api.upbit.com/v1/status/wallet"
                   {:headers (auth/make-auth-header)})
       (#(json/parse-string (:body %)))
       (map (fn [response] {:asset (get response "currency") :method (get response "net_type")}))))

(defn terms [asset method]
  (->> (client/get "https://api.upbit.com/v1/withdraws/chance"
                   {:headers (auth/make-auth-header {:currency asset :net_type method})
                    :query-params {:currency asset :net_type method}})
       (#(json/parse-string (:body %)))
       (#(make-terms (make-fee :fixed (Float/parseFloat (get-in % ["currency" "withdraw_fee"])))
                     (make-limits (make-range (Float/parseFloat (get-in % ["withdraw_limit" "minimum"]))
                                              Float/POSITIVE_INFINITY
                                              (Math/pow 0.1 (get-in % ["withdraw_limit" "fixed"])))
                                  (set (map keyword (get-in % ["currency" "wallet_support"]))))))))

(defn- response->transfer [response]
  (make-transfer (get response "uuid")
                 (get response "txid")
                 (keyword (get response "type"))
                 (make-intent "unknown"
                              (get response "net_type")
                              (get response "currency")
                              (get response "amount"))
                 (iso8601->time (get response "created_at"))
                 (get response "state")))

(defn execute-withdraw [intent]
  (let [request {:currency (asset intent)
                 :net_type (method intent)
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

(def c (chan 3))

(go-loop []
  (go-loop [info (all-methods)]
    (when (seq info)
      (>! c (first info))
      (recur (rest info))))
  (<! (timeout (* 1000 60 60)))
  (recur))

(go-loop []
  (let [x (<! c)
        manager (make-file-manager ".cache/transfer.json")]
    (manager :save (assoc (or (manager :read) {})
                          (str (:asset x) "-" (:method x))
                          (terms (:asset x) (:method x)))))
  (recur))
