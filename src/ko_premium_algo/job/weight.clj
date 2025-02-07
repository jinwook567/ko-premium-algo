(ns ko-premium-algo.job.weight
  (:require [ko-premium-algo.route.graph :refer [metadata]]
            [ko-premium-algo.strategy.terms :refer [withdraw-qty order-qty]]))

(defn edge-weight [edge]
  (let [meta (metadata edge)]
    (case (:type meta)
      :withdraw #(withdraw-qty (:base-terms meta) (:quote-terms meta) %)
      :bid #(order-qty (:terms meta) (:price meta) %)
      :ask #(* (:price meta) (order-qty (:terms meta) (:price meta) (* (:price meta) %))))))

(defn route-weight [initial route]
  (reduce #(%2 %1) initial (map edge-weight route)))
