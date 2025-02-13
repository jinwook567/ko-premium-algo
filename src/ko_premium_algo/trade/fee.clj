(ns ko-premium-algo.trade.fee
  (:refer-clojure :exclude [type])
  (:require [ko-premium-algo.lib.numeric :refer [precise]]))

(defn make-fee [type value]
  {:type type :value value})

(defn type [fee]
  (:type fee))

(defn value [fee]
  (:value fee))

(defn coerce-fee [fee n]
  (if (= (type fee) :rate)
    ((precise *) (- 1 (value fee)) n)
    ((precise -) n (value fee))))
