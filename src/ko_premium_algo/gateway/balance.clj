(ns ko-premium-algo.gateway.balance
  (:require [ko-premium-algo.binance.balance :as binance]
            [ko-premium-algo.upbit.balance :as upbit]))

(defmulti balance (fn [type & _] type))

(defmethod balance :binance [_ & args]
  (apply binance/balance args))

(defmethod balance :upbit [_ & args]
  (apply upbit/balance args))
