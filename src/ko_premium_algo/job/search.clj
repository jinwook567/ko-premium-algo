(ns ko-premium-algo.job.search
  (:require [ko-premium-algo.trade.market :refer [assets base-asset]]
            [ko-premium-algo.job.edge :refer [make-ask-edge make-bid-edge make-withdraw-edge make-node]]
            [ko-premium-algo.job.weight :refer [route-weight]]
            [ko-premium-algo.route.search :refer [make-highest-weight-route-finder]]
            [ko-premium-algo.wallet.terms :refer [fee limits]]
            [ko-premium-algo.wallet.unit :as unit]
            [ko-premium-algo.wallet.limits :refer [actions can-transfer?]]
            [ko-premium-algo.trade.fee :refer [value]]
            [ko-premium-algo.gateway.markets :refer [markets]]
            [ko-premium-algo.gateway.ticker :refer [ticker]]
            [ko-premium-algo.gateway.transfer :as transfer]
            [ko-premium-algo.trade.ticker :refer [market]]))

(def assets-to-exclude #{"TON"})

(defn has-asset? [asset market]
  (contains? (assets market) asset))

(defn remove-markets-with-assets [assets markets]
  (filter #(every? (fn [asset] (not (has-asset? asset %))) assets) markets))

(defn make-exchange-edges [exchange tickers]
  (concat (map #(make-ask-edge exchange %) tickers)
          (map #(make-bid-edge exchange %) tickers)))

(defn can-transfer-terms? [base-terms quote-terms]
  (and (some? base-terms)
       (some? quote-terms)
       (can-transfer? (actions (limits base-terms)) (actions (limits quote-terms)))))

(defn make-bridge-edges [base-exchange quote-exchange units base-terms-list quote-terms-list]
  (->> (map vector base-terms-list quote-terms-list units)
       (filter (fn [[base-terms quote-terms _]]
                 (can-transfer-terms? base-terms quote-terms)))
       (map (fn [[base-terms _ unit]]
              (make-withdraw-edge base-exchange quote-exchange (value (fee base-terms)) (unit/asset unit) (unit/method unit))))))

(defn make-one-way-graph [base-node quote-node]
  (let [tickers (fn [exchange] (ticker exchange (remove-markets-with-assets assets-to-exclude (markets exchange))))
        base-tickers (filter #(= (base-asset (market %)) (:asset base-node)) (tickers (:exchange base-node)))
        quote-tickers (filter #(= (base-asset (market %)) (:asset quote-node)) (tickers (:exchange quote-node)))
        base-edges (make-exchange-edges (:exchange base-node) base-tickers)
        quote-edges (make-exchange-edges (:exchange quote-node) quote-tickers)
        base-units (transfer/units (:exchange base-node))
        quote-units (transfer/units (:exchange quote-node))
        base-bridges (make-bridge-edges (:exchange base-node)
                                        (:exchange quote-node)
                                        base-units
                                        (transfer/terms (:exchange base-node) base-units)
                                        (transfer/terms (:exchange quote-node) base-units))
        quote-bridges (make-bridge-edges (:exchange quote-node)
                                         (:exchange base-node)
                                         quote-units
                                         (transfer/terms (:exchange quote-node) quote-units)
                                         (transfer/terms (:exchange base-node) quote-units))]
    (concat base-edges quote-edges base-bridges quote-bridges)))

(defn find-route [base-node quote-node base-node-qty]
  (let [finder (make-highest-weight-route-finder (make-one-way-graph base-node quote-node) (partial route-weight base-node-qty))
        route (concat (finder base-node quote-node) (finder quote-node base-node))
        return-qty (route-weight base-node-qty route)]
    {:return-qty return-qty
     :route route
     :rate (* 100 (/ (- return-qty base-node-qty) base-node-qty))}))

(find-route (make-node :upbit "KRW") (make-node :binance "USDT") 500000)
