(ns crypto.exchange.binance.ticker
  (:require [model.ticker :refer [make-ticker]]
            [model.market :refer [symbol]]
            [crypto.exchange.binance.lib :refer [coll->query]]
            [model.candle :refer [make-candle]]
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
                          (get % "volume")
                          (get % "quoteVolume")))))

(defn candle-ticker [markets]
  (flatten (pmap base-candle-ticker (partition-all 400 markets))))
