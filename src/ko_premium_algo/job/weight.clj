(ns ko-premium-algo.job.weight
  (:require [ko-premium-algo.route.edge :refer [metadata]]
            [ko-premium-algo.strategy.terms :refer [withdraw-qty order-qty]]))

(defn edge-weight [edge]
  (let [meta (metadata edge)]
    (if (= (:type meta) :withdraw)
      #(withdraw-qty (:base-terms meta) (:quote-terms meta) %)
      #(order-qty (:terms meta) (:price meta) %))))

(defn route-weight [initial route]
  (if (empty? route)
    0
    (reduce #(%2 %1) initial (map edge-weight route))))
