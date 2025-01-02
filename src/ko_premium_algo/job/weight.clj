(ns ko-premium-algo.job.weight
  (:require [ko-premium-algo.route.edge :refer [weight]]))

(defn edge-weight [edge]
  (if (= (:type (:meta edge)) :withdraw)
    #(- % (weight edge))
    #(/ % (weight edge))))

(defn route-weight [initial route]
  (if (empty? route)
    0
    (reduce #(%2 %1) initial (map edge-weight route))))
