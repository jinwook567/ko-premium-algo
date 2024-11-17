(ns ko-premium-algo.gateway.markets
  (:require [ko-premium-algo.binance.markets :as binance-markets]
            [ko-premium-algo.upbit.markets :as upbit-markets]))
  
(defmulti markets (fn [type] type))

(defmethod markets :binance [_]
  (binance-markets/markets))

(defmethod markets :upbit [_]
  (upbit-markets/markets))
