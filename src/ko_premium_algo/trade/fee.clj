(ns ko-premium-algo.trade.fee
  (:refer-clojure :exclude [type]))

(defn make-fee [type value]
  {:type type :value value})

(defn type [fee]
  (:type fee))

(defn value [fee]
  (:value fee))

(defn coerce-fee [fee n]
  (if (= (type fee) :rate)
    (* (- 1 (value fee)) n)
    (- n (value fee))))
