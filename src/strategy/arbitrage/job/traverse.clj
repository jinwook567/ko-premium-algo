(ns strategy.arbitrage.job.traverse
  (:require [strategy.arbitrage.route.graph :refer [metadata]]
            [strategy.arbitrage.job.terms :refer [withdraw-qty order-qty net-qty coerce-range]]
            [model.limits :refer [amount-range]]
            [model.terms :refer [limits]]))

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
