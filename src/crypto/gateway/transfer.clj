(ns crypto.gateway.transfer
  (:refer-clojure :exclude [methods])
  (:require [crypto.exchange.upbit.transfer :as upbit]
            [crypto.exchange.binance.transfer :as binance]))

(defmulti units (fn [type & _] type))

(defmethod units :upbit [_ & args]
  (apply upbit/units args))

(defmethod units :binance [_ & args]
  (apply binance/units args))

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

(defmulti deposit-address (fn [type & _] type))

(defmethod deposit-address :upbit [_ & args]
  (apply upbit/deposit-address args))

(defmethod deposit-address :binance [_ & args]
  (apply binance/deposit-address args))
