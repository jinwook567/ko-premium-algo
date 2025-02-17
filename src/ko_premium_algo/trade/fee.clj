(ns ko-premium-algo.trade.fee
  (:refer-clojure :exclude [type])
  (:require [ko-premium-algo.lib.numeric :refer [precise]]))

(defn make-fee [type deduction value]
  {:type type :deduction deduction :value value})

(defn type [fee]
  (:type fee))

(defn deduction [fee]
  (:deduction fee))

(defn value [fee]
  (:value fee))

(defn net [fee n]
  (if (= (type fee) :rate)
    ((precise *) (- 1 (value fee)) n)
    ((precise -) n (value fee))))

(defn deduct [fee n]
  (case (deduction fee)
    :additional n
    :inclusive (net fee n)))

(defn gross [fee n]
  (case (deduction fee)
    :additional (net fee n)
    :inclusive n))
