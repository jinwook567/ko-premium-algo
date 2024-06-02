(ns ko-premium-algo.market-binance
  (:require [ko-premium-algo.market :as market]
            [ko-premium-algo.coin :as coin]
            [clj-http.client :as client]
            [clojure.string :as str]
            [cheshire.core :as json]
            [ko-premium-algo.pair :as pair]
            [ko-premium-algo.distribution :as distribution]
            [ko-premium-algo.time :as time]
            [ko-premium-algo.chart :as chart]))

(defn client-get [url & req]
  (json/parse-string
   (:body (apply client/get url req))))

(defn ticker-symbol [coin-pair]
  (str/join [(coin/code (pair/quote coin-pair)) (coin/code (pair/base coin-pair))]))

(defn ticker-symbols [symbols]
  (str "[" (str/join "," (map #(str "\"" % "\"") symbols)) "]"))

(defn pairs []
  (->> (client-get "https://api.binance.com/api/v3/exchangeInfo?permissions=SPOT")
       (#(get % "symbols"))
       (filter #(= (get % "status") "TRADING"))
       (map #(pair/make-pair (get % "quoteAsset") (get % "baseAsset")))
       (map #(map coin/make-coin %))))

(defn normalize-pair [coin-pair]
  (market/normalize-pair coin-pair (pairs)))

(defn normalize-pairs [coin-pairs]
  (let [standard-pairs (pairs)]
    (map #(market/normalize-pair % standard-pairs) coin-pairs)))

(defn trade-available-coins [coin]
  (->> (pairs)
       (filter #(or (= (pair/base %) coin) (= (pair/quote %) coin)))
       (map #(if (= (pair/base %) coin) (pair/quote %) (pair/base %)))))

(defn current-exchange-rates [coin-pairs]
  (let [normalized-pairs (normalize-pairs coin-pairs)]
    (->> normalized-pairs
         (map #(ticker-symbol (market/pair %)))
         (distribution/distribute #(client-get "https://api.binance.com/api/v3/ticker/price"
                                  {:query-params {"symbols" (ticker-symbols %)}}) 500)
         (map #(Double/parseDouble (get % "price")))
         (map market/normalize-rate normalized-pairs))))

(defn io-status []
  (->> (client-get "https://api.binance.com/sapi/v1/capital/config/getall")
       (map #(get % "networkList"))
       (map (fn [networkList]
              (filter #(and (true? (get % "withdrawEnable")) (true? (get % "depositEnable"))) networkList)))
       (keep (fn [networkList]
               (or
                (some #(when-not (get % "busy") %) networkList)
                (first networkList))))))

(defn candles [coin-pair interval to count]
  (let [normalized-pair (normalize-pair coin-pair)
        normalize-rate (partial market/normalize-rate normalized-pair)
        duration (cond
                   (= interval "1m") (time/make-duration 1 "m")
                   (= interval "5m") (time/make-duration 5 "m")
                   (= interval "30m") (time/make-duration 30 "m")
                   (= interval "1h") (time/make-duration 1 "h")
                   (= interval "4h") (time/make-duration 4 "h")
                   (= interval "1d") (time/make-duration 1 "d")
                   (= interval "1w") (time/make-duration 7 "d"))
        duration-sum (reduce time/plus-duration (map (fn [_] duration) (range count)))
        request (fn [limit]
                  (client-get "https://api.binance.com/api/v3/klines"
                              {:query-params {"symbol" (ticker-symbol (market/pair normalized-pair))
                                              "startTime" (if (nil? to) nil (time/millis (time/minus-time to duration-sum)))
                                              "endTime" (time/millis to) 
                                              "interval" interval "limit" limit}}))
        distributed-request (distribution/distribute #(apply request %) 1 (distribution/distribute-number 1000 count))]
    (map
     #(chart/make-candle
       (normalize-rate (Double/parseDouble (nth % 3)))
       (normalize-rate (Double/parseDouble (nth % 1)))
       (normalize-rate (Double/parseDouble (nth % 4)))
       (normalize-rate (Double/parseDouble (nth % 2)))
       (Double/parseDouble (nth % 5)))
     distributed-request)))
