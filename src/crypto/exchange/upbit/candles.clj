(ns crypto.exchange.upbit.candles
  (:require [clj-http.client :as client]
            [model.market :as m]
            [mode.time :refer [time->iso8601]]
            [model.candle :refer [make-candle interval->map map->interval]]
            [lib.seq :refer [partition-by-size]]
            [cheshire.core :as json]))

(defn- hours-to-minutes [interval]
  (let [parsed (interval->map interval)]
    (if (= (:unit parsed) "h")
      (map->interval {:value (* (:value parsed) 60) :unit "m"})
      interval)))

(defn- candle-api-path [interval]
  (let [{:keys [unit value]} (interval->map interval)]
    (cond
      (= unit "s") "https://api.upbit.com/v1/candles/seconds"
      (= unit "m") (str "https://api.upbit.com/v1/candles/minutes/" value)
      (= unit "d") "https://api.upbit.com/v1/candles/days"
      (= unit "w") "https://api.upbit.com/v1/candles/weeks")))

(defn- base-candles [market interval to count]
  (->> (client/get (candle-api-path (hours-to-minutes interval))
                   {:query-params {:market (m/symbol market)
                                   :to (time->iso8601 to)
                                   :count count}})
       (#(json/parse-string (:body %)))
       (map  #(make-candle (get % "low_price")
                           (get % "high_price")
                           (get % "opening_price")
                           (get % "trade_price")
                           (get % "candle_acc_trade_volume")
                           (get % "candle_acc_trade_price")))))

(defn candles [market interval to count]
  (flatten (pmap #(base-candles market interval to %) (partition-by-size count 200))))
