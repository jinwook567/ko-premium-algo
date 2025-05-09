(ns strategy.arbitrage.route.search
  (:require [strategy.arbitrage.route.graph :refer [end adj eliminate]]))

(defn optimal-route [graph start-node end-node choice]
  (if (= start-node end-node)
    '()
    (->> (adj graph start-node)
         (keep #(when-let [adj-route (optimal-route (eliminate graph start-node) (end %) end-node choice)]
                  (conj adj-route %)))
         (reduce choice nil))))

(defn safe-choice [choice]
  (fn [a b]
    (if (or (nil? a) (nil? b))
      (or a b)
      (choice a b))))

(defn higher-choice [route-weight]
  (safe-choice (fn [route1 route2]
                 (max-key route-weight route1 route2))))

(defn make-route-finder [graph choice]
  (memoize (fn [start-node end-node]
             (optimal-route graph start-node end-node choice))))
