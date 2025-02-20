(ns ko-premium-algo.job.main
  (:require [ko-premium-algo.job.search :refer [make-route-finder exchange-edges link-exchange-edges]]
            [ko-premium-algo.job.traverse :refer [traverse-route]]
            [ko-premium-algo.job.signal :refer [route->signal]]
            [ko-premium-algo.route.graph :refer [edges->graph]]
            [ko-premium-algo.job.edge :refer [make-node]]
            [ko-premium-algo.strategy.execute :refer [execute-signal]]))

(defn make-arb-finder [graph]
  (let [route-finder (make-route-finder graph)]
    (fn [entry-qty & nodes]
      (let [route (apply route-finder (cons entry-qty nodes))]
        {:entry-qty entry-qty
         :return-qty (traverse-route route entry-qty)
         :route route}))))

(defn arb->signal [arb]
  (route->signal (:route arb) (:entry-qty arb)))
