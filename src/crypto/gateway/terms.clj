(ns crypto.gateway.terms
  (:require [crypto.exchange.upbit.terms :as upbit]
            [crypto.exchange.binance.terms :as binance]))

(defmulti terms (fn [type & _] type))

(defmethod terms :binance [_ & args]
  (apply binance/terms args))

(defmethod terms :upbit [_ & args]
  (apply upbit/terms args))
