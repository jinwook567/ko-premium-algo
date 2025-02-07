(ns ko-premium-algo.job.search
  (:require [ko-premium-algo.trade.market :refer [base-asset]]
            [ko-premium-algo.job.edge :refer [make-ask-edge make-bid-edge make-withdraw-edge]]
            [ko-premium-algo.job.weight :refer [route-weight]]
            [ko-premium-algo.route.search :refer [higher-choice optimal-route]]
            [ko-premium-algo.route.graph :refer [edges->graph]]
            [ko-premium-algo.gateway.markets :refer [markets]]
            [ko-premium-algo.gateway.ticker :refer [ticker]]
            [ko-premium-algo.gateway.terms :refer [terms]]
            [ko-premium-algo.gateway.transfer :as transfer]
            [clojure.math.combinatorics :as combo]))

(defn make-exchange-edges [exchange markets]
  (let [tickers (ticker exchange markets)
        terms-list (terms exchange markets)]
    (concat (map #(make-ask-edge exchange %1 %2) tickers terms-list)
            (map #(make-bid-edge exchange %1 %2) tickers terms-list))))

(defn make-bridge-edges [base-exchange quote-exchange units]
  (map #(make-withdraw-edge base-exchange quote-exchange %1 %2 %3)
       (transfer/terms base-exchange units)
       (transfer/terms quote-exchange units)
       units))

(defn link-exchanges [& exchange-list]
  (->> (combo/permuted-combinations exchange-list 2)
       (mapcat #(make-bridge-edges (first %) (second %) (transfer/units (first %))))))

(defn base-asset-edges [node]
  (->> (markets (:exchange node))
       (filter #(= (base-asset %) (:asset node)))
       (make-exchange-edges (:exchange node))))

(defn base-asset-graph [& nodes]
  (edges->graph (concat (mapcat base-asset-edges nodes)
                        (apply link-exchanges (set (map :exchange nodes))))))

(defn make-finder [graph]
  (fn find [base-node-qty base-node quote-node & next-destination-nodes]
    (let [op (optimal-route graph base-node quote-node (higher-choice (partial route-weight base-node-qty)))]
      (if (empty? next-destination-nodes)
        op
        (concat op (apply find
                          (route-weight base-node-qty op)
                          quote-node
                          (first next-destination-nodes)
                          (rest next-destination-nodes)))))))
