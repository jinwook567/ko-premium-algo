(ns model.intent
  (:require [lib.numeric :refer [number]]))

(def side-candidates
  #{:bid :ask})

(defn make-intent [market side qty price]
  (assert (side-candidates side) "Invalid side")
  {:market market :qty (number qty) :price (number price) :side side})

(defn market [intent]
  (:market intent))

(defn qty [intent]
  (:qty intent))

(defn price [intent]
  (:price intent))

(defn side [intent]
  (:side intent))

(defn amount [intent]
  (* (price intent) (qty intent)))