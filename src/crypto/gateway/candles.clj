(ns crypto.gateway.candles
  (:require [crypto.exchange.upbit.candles :as upbit]
            [crypto.exchange.binance.candles :as binance]))

(defmulti candles (fn [type & _] type))

(defmethod candles :binance [_ & args]
  (apply binance/candles args))

(defmethod candles :upbit [_ & args]
  (apply upbit/candles args))
