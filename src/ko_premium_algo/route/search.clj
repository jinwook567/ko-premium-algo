(ns ko-premium-algo.route.search
  (:require [ko-premium-algo.route.edge :refer [start end]]))

(defn- other-edges [edges edge]
  (filter #(and (not= (start edge) (start %)) (not= (start edge) (end %))) edges))

(defn- linked-edges [edges node side]
  (filter #(= (side %) node) edges))

(defn- optimal-route [edges start-node end-node choice]
  (cond
    (= start-node end-node) (list)
    (empty? edges) nil
    :else (->> (linked-edges edges start-node start)
               (keep #(when-let [route (optimal-route (other-edges edges %) (end %) end-node choice)]
                        (conj route %)))
               (reduce choice nil))))

(defn- lowest-route-choice [route-weight route1 route2]
  (min-key route-weight route1 route2))

(defn make-lowest-weight-route-finder [edges route-weight]
  (memoize (fn [start-node end-node]
             (optimal-route edges start-node end-node (partial lowest-route-choice route-weight)))))

(defn- highest-route-choice [route-weight route1 route2]
  (max-key route-weight route1 route2))

(defn make-highest-weight-route-finder [edges route-weight]
  (memoize (fn [start-node end-node]
             (optimal-route edges start-node end-node (partial highest-route-choice route-weight)))))

