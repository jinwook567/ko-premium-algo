(ns crypto.exchange.binance.markets
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [model.market :refer [make-market]]))

(defn markets []
  (->> (:body (client/get "https://api.binance.com/api/v3/exchangeInfo?permissions=SPOT"))
       json/parse-string
       (#(get % "symbols"))
       (filter #(= (get % "status") "TRADING"))
       (map #(make-market (get % "quoteAsset") (get % "baseAsset") (get % "symbol")))))
