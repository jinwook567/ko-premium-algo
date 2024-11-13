(ns ko-premium-algo.route.search
  (:require [ko-premium-algo.route.edge :refer [start end]]))

(defn- other-edges [edges edge]
  (filter #(and (not= (start edge) (start %)) (not= (start edge) (end %))) edges))

(defn- outgoing-edges [edges node]
  (filter #(= (start %) node) edges))

(defn- optimal-route [edges start-node end-node choice]
  (cond
    (= start-node end-node) (list)
    (empty? edges) nil
    :else (->> (outgoing-edges edges start-node)
               (keep #(when-let [route (optimal-route (other-edges edges %) (end %) end-node choice)]
                        (conj route %)))
               (reduce choice nil))))

(defn- route-weight [route weight]
  (if (empty? route)
    Double/POSITIVE_INFINITY
    (reduce * (map weight route))))

(defn- lowest-route-choice [weight route1 route2] 
  (if (< (route-weight route1 weight) (route-weight route2 weight)) 
    route1 route2))

(defn make-lowest-weight-route-finder [edges weight]
  (memoize (fn [start-node end-node]
                   (optimal-route edges start-node end-node (partial lowest-route-choice weight)))))
