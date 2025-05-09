(ns strategy.arbitrage.job.edge
  (:require [model.market :refer [base-asset quote-asset symbol]]
            [model.ticker :refer [market price]]
            [strategy.arbitrage.route.graph :refer [make-edge]]
            [model.limits :refer [price-range]]
            [strategy.arbitrage.job.terms :refer [coerce-range]]
            [model.terms :refer [ask-terms bid-terms limits]]
            [crypto.model.wallet.unit :refer [asset method]]))

(defn make-node [exchange asset]
  {:exchange exchange :asset asset})

(defn make-bid-edge [exchange ticker market-terms]
  (make-edge {:type :bid
              :symbol (symbol (market ticker))
              :price (coerce-range (price-range (limits (bid-terms market-terms))) (price ticker))
              :terms (bid-terms market-terms)}
             (make-node exchange (base-asset (market ticker)))
             (make-node exchange (quote-asset (market ticker)))))

(defn make-ask-edge [exchange ticker market-terms]
  (make-edge {:type :ask
              :symbol (symbol (market ticker))
              :price (coerce-range (price-range (limits (ask-terms market-terms))) (price ticker))
              :terms (ask-terms market-terms)}
             (make-node exchange (quote-asset (market ticker)))
             (make-node exchange (base-asset (market ticker)))))

(defn make-withdraw-edge [base-exchange quote-exchange base-terms quote-terms unit]
  (make-edge {:type :withdraw
              :symbol (asset unit)
              :method (method unit)
              :base-terms base-terms
              :quote-terms quote-terms}
             (make-node base-exchange (asset unit))
             (make-node quote-exchange (asset unit))))

