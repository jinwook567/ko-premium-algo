(ns ko-premium-algo.job.search
  (:require [ko-premium-algo.trade.market :refer [base-asset]]
            [ko-premium-algo.chart.candle :refer [trading-value]]
            [ko-premium-algo.job.edge :refer [make-ask-edge make-bid-edge make-withdraw-edge]]
            [ko-premium-algo.job.traverse :refer [traverse-route]]
            [ko-premium-algo.route.search :refer [higher-choice optimal-route]]
            [ko-premium-algo.route.graph :refer [metadata nodes]]
            [ko-premium-algo.gateway.markets :refer [markets]]
            [ko-premium-algo.gateway.ticker :refer [ticker candle-ticker]]
            [ko-premium-algo.gateway.terms :refer [terms]]
            [ko-premium-algo.gateway.transfer :as transfer]
            [ko-premium-algo.lib.seq :refer [percent-idx]]
            [clojure.math.combinatorics :as combo]
            [clojure.set :refer [union]]))

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

(defn bridge-edges [exchange-list]
  (->> (combo/permuted-combinations exchange-list 2)
       (mapcat #(make-bridge-edges (first %) (second %) (transfer/units (first %))))
       (filter #(some? (:base-terms (metadata %))))))

(defn make-market-candle [market candle]
  (merge market candle))

(defn make-asset-filter [side asset]
  (fn [market-candle]
    (= (side market-candle) asset)))

(defn make-trading-val-pct-filter [trading-vals pct]
  (let [sorted-vals (sort trading-vals)
        standard-val (nth sorted-vals (percent-idx pct sorted-vals))]
    (fn [market-candle]
      (>= (trading-value market-candle) standard-val))))

(defn make-trading-val-filter [standard-val]
  (fn [market-candle]
    (>= (trading-value market-candle) standard-val)))

(defn market-candle-list [exchange]
  (let [exchange-markets (markets exchange)]
    (map make-market-candle exchange-markets (candle-ticker exchange exchange-markets))))

(defn exchange-edges [exchange asset trading-val-pct trading-val]
  (let [market-candle-list (market-candle-list exchange)
        asset-filter (make-asset-filter base-asset asset)
        trading-val-pct-filter (make-trading-val-pct-filter (map trading-value market-candle-list) trading-val-pct)
        trading-val-filter (make-trading-val-filter trading-val)
        valid? (fn [market-candle]
                 (every? #(% market-candle) [asset-filter trading-val-pct-filter trading-val-filter]))]
    (make-exchange-edges exchange (filter valid? market-candle-list))))

(defn link-exchange-edges [exchange-edges]
  (concat exchange-edges
          (bridge-edges (->> exchange-edges
                             (map nodes)
                             (reduce union)
                             (map :exchange)))))

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
