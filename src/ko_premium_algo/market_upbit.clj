(ns ko-premium-algo.market-upbit
  (:require
   [ko-premium-algo.market :as market]
   [ko-premium-algo.pair :as pair]
   [clj-http.client :as client]
   [cheshire.core :as json]
   [clojure.string :as str]
   [ko-premium-algo.coin :as coin]))

(defn client-get [url & req]
  (json/parse-string
   (:body (apply client/get url req))))

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

(defn current-exchange-rates [coin-pairs]
  (let [normalized-pairs (normalize-pairs coin-pairs)]
    (->> normalized-pairs
         (map #(ticker-symbol (market/pair %)))
         (#(client-get "https://api.upbit.com/v1/ticker" {:query-params {"markets" (str/join "," %)}}))
         (map #(get % "trade_price"))
         (map market/normalize-rate normalized-pairs))))

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

(defn fee [market-pair]
  (let [coin-pair (map coin/code (market/pair market-pair))
        rate (cond
               (some #(= % "KRW") coin-pair) 0.0005
               (some #(= % "BTC") coin-pair) 0.0025
               (some #(= % "USDT") coin-pair) 0.0025
               :else Double/POSITIVE_INFINITY)]
    (* rate (market/exchange-rate market-pair))))
