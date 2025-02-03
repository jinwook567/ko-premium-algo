(ns ko-premium-algo.job.weight
  (:require [ko-premium-algo.route.edge :refer [metadata]]))

(defn edge-weight [edge]
  (if (= (:type (:meta edge)) :withdraw)
    #(- % (:price (metadata edge)))
    #(/ % (:price (metadata edge)))))

(defn route-weight [initial route]
  (if (empty? route)
    0
    (reduce #(%2 %1) initial (map edge-weight route))))
