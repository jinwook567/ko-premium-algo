(ns ko-premium-algo.job.traverse
  (:require [ko-premium-algo.route.graph :refer [metadata]]
            [ko-premium-algo.strategy.terms :refer [withdraw-qty order-qty net-qty coerce-range]]
            [ko-premium-algo.trade.limits :refer [amount-range]]
            [ko-premium-algo.trade.terms :refer [limits]]))

(defn traverse-edge [edge value]
  (let [meta (metadata edge)]
    (case (:type meta)
      :withdraw (->> (withdraw-qty (:base-terms meta) (:quote-terms meta) value)
                     (net-qty (:base-terms meta)))
      :bid (->> (order-qty (:terms meta) (:price meta) value)
                (net-qty (:terms meta)))
      :ask (->> (order-qty (:terms meta) (:price meta) (* (:price meta) value))
                (* (:price meta))
                (coerce-range (amount-range (limits (:terms meta))))
                (net-qty (:terms meta))))))

(defn traverse-route [route value]
  (reduce #(traverse-edge %2 %1) value route))
