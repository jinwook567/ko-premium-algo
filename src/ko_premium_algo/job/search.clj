(ns ko-premium-algo.job.search
  (:require [ko-premium-algo.trade.market :refer [assets base-asset]]
            [ko-premium-algo.job.edge :refer [make-ask-edge make-bid-edge make-withdraw-edge make-node]]
            [ko-premium-algo.job.weight :refer [route-weight]]
            [ko-premium-algo.route.edge :refer [nodes]]
            [ko-premium-algo.route.search :refer [make-highest-weight-route-finder]]
            [ko-premium-algo.wallet.terms :refer [fee limits]]
            [ko-premium-algo.wallet.limits :refer [actions can-transfer?]]
            [ko-premium-algo.trade.fee :refer [value]]
            [ko-premium-algo.lib.seq :refer [cartesian-product]]
            [ko-premium-algo.gateway.markets :refer [markets]]
            [ko-premium-algo.gateway.ticker :refer [ticker]]
            [ko-premium-algo.gateway.transfer :as transfer]
            [clojure.set :refer [union]]
            [ko-premium-algo.trade.ticker :refer [market]]))

(def assets-to-exclude #{"TON"})

(defn has-asset? [asset market]
  (contains? (assets market) asset))

(defn remove-markets-with-assets [assets markets]
  (filter #(every? (fn [asset] (not (has-asset? asset %))) assets) markets))

(defn make-exchange-edges [exchange tickers]
  (concat (map #(make-ask-edge exchange %) tickers)
          (map #(make-bid-edge exchange %) tickers)))

(defn add-method [method terms]
  (merge {:method method} terms))

(defn can-transfer-terms-list [base-method-terms-list quote-method-terms-list]
  (->> (cartesian-product base-method-terms-list quote-method-terms-list)
       (filter #(= (:method (first %)) (:method (second %))))
       (filter #(can-transfer? (actions (limits (first %))) (actions (limits (second %)))))
       (map first)))

(defn make-bridge-edge [base-exchange quote-exchange transferable-method-terms-list asset]
  (->> transferable-method-terms-list
       (map (fn [terms] {:fee (value (fee terms)) :method (:method terms)}))
       (reduce #(min-key :fee %1 %2) {:fee Double/POSITIVE_INFINITY :method nil})
       (#(make-withdraw-edge base-exchange quote-exchange (:fee %) asset (:method %)))))

(defn make-bridge-edges [base-exchange quote-exchange base-method-terms-list quote-method-terms-list asset]
  (list (make-bridge-edge base-exchange quote-exchange (can-transfer-terms-list base-method-terms-list quote-method-terms-list) asset)
        (make-bridge-edge quote-exchange base-exchange (can-transfer-terms-list quote-method-terms-list base-method-terms-list) asset)))

(defn make-fake-bridge-edges [base-exchange quote-exchange asset]
  (list (make-withdraw-edge base-exchange quote-exchange 0 asset :fake)
        (make-withdraw-edge quote-exchange base-exchange  0 asset :fake)))

(defn make-one-way-graph [base-node quote-node]
  (let [tickers (fn [exchange] (ticker exchange (remove-markets-with-assets assets-to-exclude (markets exchange))))
        base-tickers (filter #(= (base-asset (market %)) (:asset base-node)) (tickers (:exchange base-node)))
        quote-tickers (filter #(= (base-asset (market %)) (:asset quote-node)) (tickers (:exchange quote-node)))
        base-edges (make-exchange-edges (:exchange base-node) base-tickers)
        quote-edges (make-exchange-edges (:exchange quote-node) quote-tickers)
        method-terms-list (fn [node] (map #(add-method % (transfer/terms (:exchange node) (:asset node) %))
                                          (transfer/methods (:exchange node) (:asset node))))
        bridge-edges (->> [base-edges quote-edges]
                          (map #(apply union (map nodes %)))
                          (apply cartesian-product)
                          (filter #(= (:asset (first %)) (:asset (second %))))
                          (map #(make-fake-bridge-edges (:exchange base-node) (:exchange quote-node) (:asset (first %))))
                          flatten)]
    (concat base-edges quote-edges bridge-edges)))

(defn find-route [base-node quote-node base-node-qty]
  (let [finder (make-highest-weight-route-finder (make-one-way-graph base-node quote-node) (partial route-weight base-node-qty))
        route (concat (finder base-node quote-node) (finder quote-node base-node))
        return-qty (route-weight base-node-qty route)]
    {:return-qty return-qty
     :route route
     :rate (* 100 (/ (- return-qty base-node-qty) base-node-qty))}))

(find-route (make-node :upbit "KRW") (make-node :binance "USDT") 100000000)
