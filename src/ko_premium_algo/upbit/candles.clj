(ns ko-premium-algo.upbit.candles
  (:require [clj-http.client :as client]
            [ko-premium-algo.trade.market :as m]
            [ko-premium-algo.lib.time :refer [iso8601]]
            [ko-premium-algo.chart.candle :refer [make-candle]]
            [ko-premium-algo.lib.partition :refer [partition-by-size]]
            [cheshire.core :as json]))

(defn- interval->map [interval]
  (let [[_ value unit] (re-matches #"(\d+)([a-zA-Z]+)" (name interval))]
    {:value (Integer. value)
     :unit unit}))

(defn- map->interval [interval-map]
  (keyword (str (:value interval-map) (:unit interval-map))))

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
                                   :to (iso8601 to)
                                   :count count}})
       (#(json/parse-string (:body %)))
       (map  #(make-candle (get % "low_price")
                           (get % "high_price")
                           (get % "opening_price")
                           (get % "trade_price")
                           (get % "candle_acc_trade_volume")))))

(defn candles [market interval to count]
  (flatten (pmap #(base-candles market interval to %) (partition-by-size count 200))))
