(ns ko-premium-algo.gateway.terms
  (:require [ko-premium-algo.upbit.terms :as upbit]
            [ko-premium-algo.binance.terms :as binance]))

(defmulti terms (fn [type & _] type))

(defmethod terms :binance [_ & args]
  (apply binance/terms args))

(defmethod terms :upbit [_ & args]
  (apply upbit/terms args))
