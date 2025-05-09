(ns model.ticker
  (:require [lib.numeric :refer [number]]))

(defn make-ticker [market price]
  {:market market :price (number price)})

(defn market [ticker]
  (:market ticker))

(defn price [ticker]
  (:price ticker))
