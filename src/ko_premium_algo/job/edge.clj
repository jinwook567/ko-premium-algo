(ns ko-premium-algo.job.edge
  (:require [ko-premium-algo.trade.market :refer [base-asset quote-asset symbol]]
            [ko-premium-algo.trade.ticker :refer [market price]]
            [ko-premium-algo.route.edge :refer [make-edge]]))

(defn make-node [exchange asset]
  {:exchange exchange :asset asset})

(defn make-bid-edge [exchange ticker]
  (make-edge {:type :bid
              :symbol (symbol (market ticker))
              :price (price ticker)}
             (make-node exchange (base-asset (market ticker)))
             (make-node exchange (quote-asset (market ticker)))))

(defn make-ask-edge [exchange ticker]
  (make-edge {:type :ask
              :symbol (symbol (market ticker))
              :price (/ 1 (price ticker))}
             (make-node exchange (quote-asset (market ticker)))
             (make-node exchange (base-asset (market ticker)))))

(defn make-withdraw-edge [base-exchange quote-exchange fee asset method]
  (make-edge {:type :withdraw
              :symbol asset
              :method method
              :price fee}
             (make-node base-exchange asset)
             (make-node quote-exchange asset)))
