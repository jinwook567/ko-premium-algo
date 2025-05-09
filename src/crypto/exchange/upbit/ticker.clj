(ns crypto.exchange.upbit.ticker
  (:require [model.ticker :refer [make-ticker]]
            [model.market :refer [symbol]]
            [model.candle :refer [make-candle]]
            [cheshire.core :as json]
            [clojure.string :as str]
            [clj-http.client :as client]))

(defn ticker [markets]
  (->> (map symbol markets)
       (#(client/get "https://api.upbit.com/v1/ticker" {:query-params {"markets" (str/join "," %)}}))
       (#(json/parse-string (:body %)))
       (map #(make-ticker %1 (get %2 "trade_price")) markets)))

(defn candle-ticker [markets]
  (->> (client/get "https://api.upbit.com/v1/ticker"
                   {:query-params {"markets" (str/join "," (map symbol markets))}})
       (#(json/parse-string (:body %)))
       (map #(make-candle (get % "low_price")
                          (get % "high_price")
                          (get % "opening_price")
                          (get % "trade_price")
                          (get % "acc_trade_volume_24h")
                          (get % "acc_trade_price_24h")))))
