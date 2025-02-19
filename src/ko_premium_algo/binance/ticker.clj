(ns ko-premium-algo.binance.ticker
  (:require [ko-premium-algo.trade.ticker :refer [make-ticker]]
            [ko-premium-algo.trade.market :refer [symbol]]
            [ko-premium-algo.binance.lib :refer [coll->query]]
            [ko-premium-algo.chart.candle :refer [make-candle]]
            [cheshire.core :as json]
            [clj-http.client :as client]))

(defn base-ticker [markets]
  (->> (map symbol markets)
       (#(client/get "https://api.binance.com/api/v3/ticker/price" {:query-params {"symbols" (coll->query %)}}))
       (#(json/parse-string (:body %)))
       (map #(make-ticker %1 (get %2 "price")) markets)))

(defn ticker [markets]
  (flatten (pmap base-ticker (partition-all 400 markets))))

(defn base-candle-ticker [markets]
  (->> (client/get "https://api.binance.com/api/v3/ticker/24hr"
                   {:query-params {"symbols" (coll->query (map symbol markets))}})
       (#(json/parse-string (:body %)))
       (map #(make-candle (get % "lowPrice")
                          (get % "highPrice")
                          (get % "openPrice")
                          (get % "lastPrice")
                          (get % "volumn")
                          (get % "quoteVolume")))))

(defn candle-ticker [markets]
  (flatten (pmap base-candle-ticker (partition-all 400 markets))))
