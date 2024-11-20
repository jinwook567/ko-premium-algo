(ns ko-premium-algo.gateway.ticker
  (:require [ko-premium-algo.binance.ticker :as binance]
            [ko-premium-algo.upbit.ticker :as upbit]))

(defmulti ticker (fn [type & _] type))

(defmethod ticker :binance [_ & args]
  (apply binance/ticker args))

(defmethod ticker :upbit [_ & args]
  (apply upbit/ticker args))