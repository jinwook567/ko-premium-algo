(ns strategy.arbitrage.job.search
  (:require [model.market :refer [base-asset]]
            [model.candle :refer [trading-value]]
            [strategy.arbitrage.job.edge :refer [make-ask-edge make-bid-edge make-withdraw-edge]]
            [strategy.arbitrage.job.traverse :refer [traverse-route]]
            [strategy.arbitrage.route.search :refer [higher-choice optimal-route]]
            [strategy.arbitrage.route.graph :refer [metadata nodes]]
            [crypto.gateway.markets :refer [markets]]
            [crypto.gateway.ticker :refer [ticker candle-ticker]]
            [crypto.gateway.terms :refer [terms]]
            [crypto.gateway.transfer :as transfer]
            [lib.seq :refer [percent-idx]]
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
        trading-val-pct-filter #(make-trading-val-pct-filter (map trading-value %) trading-val-pct)
        trading-val-filter (make-trading-val-filter trading-val)]
    (->> market-candle-list
         (filter asset-filter)
         (#(filter (trading-val-pct-filter %) %))
         (filter trading-val-filter)
         (make-exchange-edges exchange))))

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
