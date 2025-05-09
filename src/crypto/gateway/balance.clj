(ns crypto.gateway.balance
  (:require [crypto.exchange.binance.balance :as binance]
            [crypto.exchange.upbit.balance :as upbit]))

(defmulti balance (fn [type & _] type))

(defmethod balance :binance [_ & args]
  (apply binance/balance args))

(defmethod balance :upbit [_ & args]
  (apply upbit/balance args))
