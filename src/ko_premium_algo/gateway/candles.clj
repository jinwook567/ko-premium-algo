(ns ko-premium-algo.gateway.candles
  (:require [ko-premium-algo.upbit.candles :as upbit]
            [ko-premium-algo.binance.candles :as binance]))

(defmulti candles (fn [type & _] type))

(defmethod candles :binance [_ & args]
  (apply binance/candles args))

(defmethod candles :upbit [_ & args]
  (apply upbit/candles args))
