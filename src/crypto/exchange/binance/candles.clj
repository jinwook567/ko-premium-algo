(ns crypto.exchange.binance.candles
  (:require [clj-http.client :as client]
            [model.market :as m]
            [mode.time :refer [make-duration minus-time time->millis]]
            [model.candle :refer [make-candle interval->map]]
            [lib.seq :refer [partition-by-size]]
            [cheshire.core :as json]))

(defn- start-time [interval to count]
  (let [{:keys [unit value]} (interval->map interval)]
    (minus-time to (make-duration (* count value) unit))))

(defn- base-candles [market interval to count]
  (->> (client/get "https://api.binance.com/api/v3/klines"
                   {:query-params {:symbol (m/symbol market)
                                   :interval (name interval)
                                   :startTime (time->millis (start-time interval to count))
                                   :endTime (time->millis to)
                                   :limit count}})
       (#(json/parse-string (:body %)))
       (map #(make-candle (nth % 3) (nth % 2) (nth % 1) (nth % 4) (nth % 5) (nth % 7)))))

(defn candles [market interval to count]
  (flatten (pmap #(base-candles market interval to %) (partition-by-size count 1000))))
