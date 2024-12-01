(ns ko-premium-algo.job.search
  (:require [ko-premium-algo.gateway.markets :refer [markets]]
            [ko-premium-algo.gateway.ticker :refer [ticker]]
            [ko-premium-algo.trade.market :refer [base-asset quote-asset symbol]]
            [ko-premium-algo.trade.ticker :refer [market price]]
            [ko-premium-algo.route.edge :refer [make-edge weight]]
            [ko-premium-algo.route.search :refer [make-lowest-weight-route-finder]]))

(defn- make-node [exchange asset]
  {:exchange exchange :asset asset})

(defn- make-bid-edge [exchange ticker]
  (merge {:meta {:type :bid
                 :symbol (symbol (market ticker))}}
         (make-edge (price ticker)
                    (make-node exchange (base-asset (market ticker)))
                    (make-node exchange (quote-asset (market ticker))))))

(defn- make-ask-edge [exchange ticker]
  (merge {:meta {:type :ask
                 :symbol (symbol (market ticker))}}
         (make-edge (/ 1 (price ticker))
                    (make-node exchange (quote-asset (market ticker)))
                    (make-node exchange (base-asset (market ticker))))))

(defn- make-withdraw-edge [base-exchange quote-exchange fee asset]
  (merge {:meta {:type :withdraw
                 :symbol asset}}
         (make-edge fee
                    (make-node base-exchange asset)
                    (make-node quote-exchange asset))))

(def ^:private black-list #{"TON"})

(defn- exclude-black-list [markets]
  (remove #(or (contains? black-list (base-asset %))
               (contains? black-list (quote-asset %))) markets))

(defn- node-tickers [node]
  (ticker (:exchange node)
          (exclude-black-list (filter #(= (base-asset %) (:asset node)) (markets (:exchange node))))))

(defn- quote-assets [tickers]
  (map #(quote-asset (market %)) tickers))

;; withdraw 관련 처리 필요, weight 함수 edge가 아닌 route를 받도록 수정 필요
(defn- calculate-weight [edge]
  (weight edge))

(defn candidate-route [base-node quote-node]
  ((make-lowest-weight-route-finder (concat (map #(make-bid-edge (:exchange base-node) %)
                                                 (node-tickers base-node))
                                            (map #(make-withdraw-edge (:exchange base-node) (:exchange quote-node) 1 %)
                                                 (quote-assets (node-tickers base-node)))
                                            (map #(make-ask-edge (:exchange quote-node) %)
                                                 (node-tickers quote-node)))
                                    calculate-weight) base-node quote-node))

(candidate-route (make-node :upbit "KRW") (make-node :binance "USDT"))
