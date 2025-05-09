(ns model.market
  (:refer-clojure :exclude [symbol]))

(defn make-market [base-asset quote-asset symbol]
  {:base-asset base-asset :quote-asset quote-asset :symbol symbol})

(defn base-asset [market]
  (:base-asset market))

(defn quote-asset [market]
  (:quote-asset market))

(defn symbol [market]
  (:symbol market))

(defn assets [market]
  #{(base-asset market) (quote-asset market)})
