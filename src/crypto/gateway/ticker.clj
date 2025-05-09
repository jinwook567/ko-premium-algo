(ns crypto.gateway.ticker
  (:require [crypto.exchange.binance.ticker :as binance]
            [crypto.exchange.upbit.ticker :as upbit]))

(defmulti ticker (fn [type & _] type))

(defmethod ticker :binance [_ & args]
  (apply binance/ticker args))

(defmethod ticker :upbit [_ & args]
  (apply upbit/ticker args))

(defmulti candle-ticker (fn [type & _] type))

(defmethod candle-ticker :binance [_ & args]
  (apply binance/candle-ticker args))

(defmethod candle-ticker :upbit [_ & args]
  (apply upbit/candle-ticker args))
