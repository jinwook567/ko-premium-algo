(ns ko-premium-algo.binance.candles
  (:require [clj-http.client :as client]
            [ko-premium-algo.trade.market :as m]
            [ko-premium-algo.lib.time :refer [make-duration minus-time millis]]
            [ko-premium-algo.chart.candle :refer [make-candle interval->map]]
            [ko-premium-algo.lib.partition :refer [partition-by-size]]
            [cheshire.core :as json]))

(defn- start-time [interval to count]
  (let [{:keys [unit value]} (interval->map interval)]
    (minus-time to (make-duration (* count value) unit))))

(defn- base-candles [market interval to count]
  (->> (client/get "https://api.binance.com/api/v3/klines"
                   {:query-params {:symbol (m/symbol market)
                                   :interval (name interval)
                                   :startTime (millis (start-time interval to count))
                                   :endTime (millis to)
                                   :limit count}})
       (#(json/parse-string (:body %)))
       (map #(apply make-candle (map Float/parseFloat [(nth % 3) (nth % 2) (nth % 1) (nth % 4) (nth % 5)])))))

(defn candles [market interval to count]
  (flatten (pmap #(base-candles market interval to %) (partition-by-size count 1000))))
