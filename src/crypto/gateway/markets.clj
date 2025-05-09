(ns crypto.gateway.markets
  (:require [crypto.exchange.binance.markets :as binance-markets]
            [crypto.exchange.upbit.markets :as upbit-markets]))

(defmulti markets (fn [type] type))

(defmethod markets :binance [_]
  (binance-markets/markets))

(defmethod markets :upbit [_]
  (upbit-markets/markets))
