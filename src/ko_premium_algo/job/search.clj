(ns ko-premium-algo.job.search
  (:require [ko-premium-algo.trade.market :refer [assets base-asset]]
            [ko-premium-algo.job.edge :refer [make-ask-edge make-bid-edge make-withdraw-edge]]
            [ko-premium-algo.job.weight :refer [route-weight]]
            [ko-premium-algo.route.search :refer [make-highest-weight-route-finder]]
            [ko-premium-algo.wallet.terms :refer [fee limits]]
            [ko-premium-algo.wallet.unit :as unit]
            [ko-premium-algo.wallet.limits :refer [actions can-transfer?]]
            [ko-premium-algo.trade.fee :refer [value]]
            [ko-premium-algo.gateway.markets :refer [markets]]
            [ko-premium-algo.gateway.ticker :refer [ticker]]
            [ko-premium-algo.gateway.terms :refer [terms]]
            [ko-premium-algo.gateway.transfer :as transfer]
            [ko-premium-algo.trade.ticker :refer [market]]
            [clojure.math.combinatorics :as combo]))

;; terms api 변경 필요
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

(defn all-bridge-edges [& exchange-list]
  (->> (combo/permuted-combinations exchange-list 2)
       (mapcat #(make-bridge-edges (first %) (second %) (transfer/units (first %))))))

(defn base-asset-exchange-edges [node]
  (->> (market (:exchange node))
       (filter #(= (base-asset %) (:asset node)))
       (make-exchange-edges (:exchange node))))

(defn base-asset-edges [& nodes]
  (concat (mapcat base-asset-exchange-edges nodes)
          (apply all-bridge-edges (set (map :exchange nodes)))))
