(ns strategy.arbitrage.job.main
  (:require [strategy.arbitrage.job.search :refer [make-route-finder exchange-edges link-exchange-edges]]
            [strategy.arbitrage.job.traverse :refer [traverse-route]]
            [strategy.arbitrage.job.signal :refer [route->signal]]
            [strategy.arbitrage.route.graph :refer [edges->graph]]
            [strategy.arbitrage.job.edge :refer [make-node]]
            [strategy.arbitrage.job.execute :refer [execute-signal]]))

(defn make-arb-finder [graph]
  (let [route-finder (make-route-finder graph)]
    (fn [entry-qty & nodes]
      (let [route (apply route-finder (cons entry-qty nodes))]
        {:entry-qty entry-qty
         :return-qty (traverse-route route entry-qty)
         :route route}))))

(defn arb->signal [arb]
  (route->signal (:route arb) (:entry-qty arb)))
