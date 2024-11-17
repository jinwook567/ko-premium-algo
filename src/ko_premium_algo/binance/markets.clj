(ns ko-premium-algo.binance.markets
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [ko-premium-algo.trade.market :refer [make-market]]))

(defn markets []
  (->> (:body (client/get "https://api.binance.com/api/v3/exchangeInfo?permissions=SPOT"))
       json/parse-string
       (#(get % "symbols"))
       (filter #(= (get % "status") "TRADING"))
       (map #(make-market (get % "baseAsset") (get % "quoteAsset") (get % "symbol")))))
