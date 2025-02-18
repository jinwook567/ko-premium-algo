(ns ko-premium-algo.job.search
  (:require [ko-premium-algo.trade.market :refer [base-asset]]
            [ko-premium-algo.chart.candle :refer [trading-value]]
            [ko-premium-algo.job.edge :refer [make-ask-edge make-bid-edge make-withdraw-edge]]
            [ko-premium-algo.job.traverse :refer [traverse-route]]
            [ko-premium-algo.route.search :refer [higher-choice optimal-route]]
            [ko-premium-algo.route.graph :refer [edges->graph metadata]]
            [ko-premium-algo.gateway.markets :refer [markets]]
            [ko-premium-algo.gateway.ticker :refer [ticker candle-ticker]]
            [ko-premium-algo.gateway.terms :refer [terms]]
            [ko-premium-algo.gateway.transfer :as transfer]
            [ko-premium-algo.lib.seq :refer [percent-idx]]
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
       (mapcat #(make-bridge-edges (first %) (second %) (transfer/units (first %))))
       (filter #(some? (:base-terms (metadata %))))))

(defn base-asset-markets [node]
  (->> (markets (:exchange node))
       (filter #(= (base-asset %) (:asset node)))))

(defn liquid-markets [exchange markets]
  (->> (candle-ticker exchange markets)
       (map vector markets)
       (sort-by #(trading-value (second %)))
       (map first)
       (#(drop (inc (percent-idx 30 %)) %))))

(defn linked-liquid-edges [node]
  (->> (base-asset-markets node)
       (liquid-markets (:exchange node))
       (make-exchange-edges (:exchange node))))

(defn base-asset-graph [& nodes]
  (edges->graph (concat (mapcat linked-liquid-edges nodes)
                        (apply link-exchanges (set (map :exchange nodes))))))

(defn make-route-finder [graph]
  (fn find [base-node-qty base-node quote-node & next-destination-nodes]
    (let [op (optimal-route graph base-node quote-node (higher-choice (fn [route] (traverse-route route base-node-qty))))]
      (if (empty? next-destination-nodes)
        op
        (concat op (apply find
                          (traverse-route op base-node-qty)
                          quote-node
                          (first next-destination-nodes)
                          (rest next-destination-nodes)))))))
