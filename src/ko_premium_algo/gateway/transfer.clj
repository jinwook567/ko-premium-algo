(ns ko-premium-algo.gateway.transfer
  (:refer-clojure :exclude [methods])
  (:require [ko-premium-algo.upbit.transfer :as upbit]
            [ko-premium-algo.binance.transfer :as binance]))

(defmulti methods (fn [type & _] type))

(defmethod methods :upbit [_ & args]
  (apply upbit/methods args))

(defmethod methods :binance [_ & args]
  (apply binance/methods args))

(defmulti terms (fn [type & _] type))

(defmethod terms :upbit [_ & args]
  (apply upbit/terms args))

(defmethod terms :binance [_ & args]
  (apply binance/terms args))

(defmulti transfer (fn [type & _] type))

(defmethod transfer :upbit [_ & args]
  (apply upbit/transfer args))

(defmethod transfer :binance [_ & args]
  (apply binance/transfer args))

(defmulti execute-withdraw (fn [type & _] type))

(defmethod execute-withdraw :upbit [_ & args]
  (apply upbit/execute-withdraw args))

(defmethod execute-withdraw :binance [_ & args]
  (apply binance/execute-withdraw args))