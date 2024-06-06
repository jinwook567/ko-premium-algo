(ns ko-premium-algo.market-upbit
  (:require [ko-premium-algo.market :as market]
            [ko-premium-algo.pair :as pair]
            [clj-http.client :as client]
            [cheshire.core :as json]
            [clojure.string :as str]
            [ko-premium-algo.coin :as coin]
            [ko-premium-algo.distribution :as distribution]
            [ko-premium-algo.time :as time]
            [ko-premium-algo.chart :as chart]
            [buddy.core.hash :as hash]
            [buddy.sign.jwt :as jwt]
            [environ.core :refer [env]]))

(defn client-get
  ([url] (json/parse-string (:body (client/get url))))
  ([url req] (json/parse-string (:body (client/get url req)))))

(defn token [query]
  (-> {:access_key (env :upbit-access-key) :nonce (str (java.util.UUID/randomUUID))}
      (merge (if (some? query)
               {:query_hash (str/join (hash/sha512 (client/generate-query-string query)))
                :query_hash_alg "SHA512"}
               nil))
      (jwt/sign (env :upbit-secret-key))
      (#(str "Bearer " %))))

(defn add-token [req type]
  (assoc-in req [:headers :Authorization] (token ((if (= type :get) :query-params :body) req))))

(defn client-get-with-secret
  ([url] (client-get url (add-token nil :get)))
  ([url req] (client-get url (add-token req :get))))

(defn ticker-symbol [coin-pair]
  (str/join "-" [(coin/code (pair/base coin-pair)) (coin/code (pair/quote coin-pair))]))

(defn pairs []
  (->> (client-get "https://api.upbit.com/v1/market/all")
       (map #(get % "market"))
       (map #(str/split % #"-"))
       (map #(pair/make-pair (first %) (second %)))
       (map #(map coin/make-coin %))))

(defn trade-available-coins [coin]
  (->> (pairs)
       (filter #(or (= (pair/base %) coin) (= (pair/quote %) coin)))
       (map #(if (= (pair/base %) coin) (pair/quote %) (pair/base %)))))

(defn normalize-pair [coin-pair]
  (market/normalize-pair coin-pair (pairs)))

(defn normalize-pairs [coin-pairs]
  (let [standard-pairs (pairs)]
    (map #(market/normalize-pair % standard-pairs) coin-pairs)))

(defn side [normalized-pair]
  (if (market/reversed-pair? normalized-pair) "ask" "bid"))

(defn current-exchange-rates [coin-pairs]
  (let [normalized-pairs (normalize-pairs coin-pairs)]
    (->> normalized-pairs
         (map #(ticker-symbol (market/pair %)))
         (#(client-get "https://api.upbit.com/v1/ticker" {:query-params {"markets" (str/join "," %)}}))
         (map #(get % "trade_price"))
         (map market/normalize-rate normalized-pairs))))

(defn order-book [coin-pairs]
  (let [normalized-pairs (normalize-pairs coin-pairs)]
    (->> normalized-pairs
         (map #(ticker-symbol (market/pair %)))
         (#(client-get "https://api.upbit.com/v1/orderbook" {:query-params {"markets" (str/join "," %)}}))
         (map #(get % "orderbook_units"))
         (map (fn [pair units]
                (map #(chart/make-order-book-item
                       (market/normalize-rate pair (get % (str (side pair) "_price")))
                       (get % (str (side pair) "_size"))) units))
              normalized-pairs))))

(defn io-status []
  (->> (client-get "https://api.upbit.com/v1/status/wallet")
       (map #(let [wallet-state (get % "wallet_state") block-state (get % "block_state")]
               (coin/make-io-status-item
                (coin/make-coin (get % "currency"))
                (and (or (= wallet-state "working") (= wallet-state "deposit_only")) (not= block-state "inactive"))
                (and (or (= wallet-state "working") (= wallet-state "withdraw_only")) (not= block-state "inactive"))
                (get % "net_type")
                (get % "network_name")
                (or (= block-state "delayed") (= block-state "inactive")))))))


(defn candles [coin-pair interval to count] 
  (let [normalized-pair (normalize-pair coin-pair)
        normalize-rate (partial market/normalize-rate normalized-pair)
        request (fn [type limit]
                  (client-get (str/join "/" (cons "https://api.upbit.com/v1/candles" type))
                              {:query-params {"market" (ticker-symbol (market/pair normalized-pair)) "to" (time/iso8601 to) "count" limit}}))
        distributed-request (fn [type] 
                              (distribution/distribute #(request type (first %)) 1 (distribution/distribute-number 200 count)))
        request-type (cond
                       (= interval "1m") ["minutes" 1]
                       (= interval "5m") ["minutes" 5]
                       (= interval "30m") ["minutes" 30]
                       (= interval "1h") ["minutes" 60]
                       (= interval "4h") ["minutes" 240]
                       (= interval "1d") ["days"]
                       (= interval "1w") ["weeks"])]
    (map #(chart/make-candle
           (normalize-rate (get % "low_price"))
           (normalize-rate (get % "opening_price"))
           (normalize-rate (get % "trade_price"))
           (normalize-rate (get % "high_price"))
           (get % "candle_acc_trade_volume"))
         (distributed-request request-type))))  (let [normalized-pair (normalize-pair coin-pair)]
(defn exchange-info [coin-pair]
  (let [normalized-pair (normalize-pair coin-pair)]
    (->> (ticker-symbol (market/pair normalized-pair))
         (#(client-get-with-secret (str "https://api.upbit.com/v1/orders/chance?market=" %)))
         (#(market/make-exchange-info 
            (get-in % ["market" (side normalized-pair) "min_total"]) 
            (get-in % ["market" "max_total"]) 
            (get-in % ["market" "state"])
            (get % (str (side normalize-pair) "_fee")))))))

(defn trades [coin-pair to count]
  (let [normalized-pair (normalize-pair coin-pair)
        request (fn [max-count]
                  (client-get "https://api.upbit.com/v1/trades/ticks"
                              {:query-params {"market" (ticker-symbol (market/pair normalized-pair))
                                              "to" (when to (time/hhmmss to))
                                              "daysAgo" (when to (time/days (time/diff to (time/now))))
                                              "count" max-count}}))
        response (distribution/distribute request 1 (distribution/distribute-number 500 count))]
    (map #(chart/make-trade
           (market/normalize-rate normalized-pair (get % "trade_price"))
           (get % "trade_volume")
           (time/millis-to-time (get % "timestamp"))) 
         response)))
