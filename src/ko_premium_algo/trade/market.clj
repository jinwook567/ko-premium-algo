(ns ko-premium-algo.trade.market
  (:refer-clojure :exclude [symbol])
  (:require [ko-premium-algo.pair :refer [make-pair]]))

(defn make-market [base-asset quote-asset symbol]
  {:base-asset base-asset :quote-asset quote-asset :symbol symbol})

(defn base-asset [market]
  (:base-asset market))

(defn quote-asset [market]
  (:quote-asset market))

(defn symbol [market]
  (:symbol market))

(defn pair [market]
  (make-pair (base-asset market) (quote-asset market)))