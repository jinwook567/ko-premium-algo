(ns ko-premium-algo.trade.ticker
  (:require [ko-premium-algo.lib.numeric :refer [number]]))

(defn make-ticker [market price]
  {:market market :price (number price)})

(defn market [ticker]
  (:market ticker))

(defn price [ticker]
  (:price ticker))
