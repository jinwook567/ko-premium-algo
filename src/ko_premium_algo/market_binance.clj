(ns ko-premium-algo.market-binance
  (:require [ko-premium-algo.market :as market]
            [ko-premium-algo.coin :as coin]
            [clj-http.client :as client]
            [clojure.string :as str]
            [cheshire.core :as json]
            [ko-premium-algo.pair :as pair]
            [ko-premium-algo.distribution :refer [distribute]]
            ))

(defn client-get [url & req]
  (json/parse-string
   (:body (apply client/get url req))))

(defn ticker-symbol [base-coin quote-coin]
  (str/join [(coin/code quote-coin) (coin/code base-coin)]))

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
         (map #(apply ticker-symbol (market/pair %)))
         (distribute #(client-get "https://api.binance.com/api/v3/ticker/price"
                       {:query-params {"symbols" (ticker-symbols %)}}) 500)
         (map #(Double/parseDouble (get % "price")))
         (map (fn [pair price] 
                (if (market/reversed-pair? pair) (/ 1 price) price)) 
              normalized-pairs))))

(defn io-status []
  (->> (client-get "https://api.binance.com/sapi/v1/capital/config/getall")
       (map #(get % "networkList"))
       (map (fn [networkList]
              (filter #(and (true? (get % "withdrawEnable")) (true? (get % "depositEnable"))) networkList)))
       (keep (fn [networkList]
               (or
                (some #(when-not (get % "busy") %) networkList)
                (first networkList))))))

(defn fee [market-pair]
  (* (market/exchange-rate market-pair) 0.001))
