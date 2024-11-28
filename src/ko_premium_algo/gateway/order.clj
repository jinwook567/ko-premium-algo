(ns ko-premium-algo.gateway.order
  (:require [ko-premium-algo.binance.order :as binance]
            [ko-premium-algo.upbit.order :as upbit]))

(defmulti execute-order (fn [type & _] type))

(defmethod execute-order :binance [_ & args]
  (apply binance/execute-order args))

(defmethod execute-order :upbit [_ & args]
  (apply upbit/execute-order args))

(defmulti open-orders (fn [type & _] type))

(defmethod open-orders :binance [_ & args]
  (apply binance/open-orders args))

(defmethod open-orders :upbit [_ & args]
  (apply upbit/open-orders args))

(defmulti order (fn [type & _] type))

(defmethod order :binance [_ & args]
  (apply binance/order args))

(defmethod order :upbit [_ & args]
  (apply upbit/order args))