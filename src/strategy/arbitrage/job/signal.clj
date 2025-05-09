(ns strategy.arbitrage.job.signal
  (:require [model.signal :refer [make-operation make-signal]]
            [strategy.arbitrage.route.graph :refer [start end metadata]]
            [model.intent :refer [make-intent]]
            [model.market :refer [make-market]]
            [crypto.model.wallet.intent :as wallet-intent]
            [crypto.model.wallet.unit :refer [make-unit]]
            [strategy.arbitrage.job.terms :refer [order-qty withdraw-qty]]
            [strategy.arbitrage.job.traverse :refer [traverse-edge]]
            [crypto.gateway.transfer :refer [deposit-address]]
            [strategy.arbitrage.job.execute :refer [make-withdraw-intent]]))

(defn- order-edge? [edge]
  (let [type (:type (metadata edge))]
    (or (= type :ask) (= type :bid))))

(defn- ask-edge->intent [edge qty]
  (make-intent (make-market (:asset (end edge))
                            (:asset (start edge))
                            (:symbol (metadata edge)))
               (:type (metadata edge))
               (order-qty (:terms (metadata edge)) (:price (metadata edge)) (* (:price (metadata edge)) qty))
               (:price (metadata edge))))

(defn- bid-edge->intent [edge qty]
  (make-intent (make-market (:asset (start edge))
                            (:asset (end edge))
                            (:symbol (metadata edge)))
               (:type (metadata edge))
               (order-qty (:terms (metadata edge)) (:price (metadata edge)) qty)
               (:price (metadata edge))))

(defn- edge->order [edge qty]
  (make-operation :order
                  (:exchange (start edge))
                  (if (= (:type (metadata edge)) :bid)
                    (bid-edge->intent edge qty)
                    (ask-edge->intent edge qty))))

(defn- edge->withdraw [edge qty]
  (make-operation :withdraw
                  (:exchange (start edge))
                  (make-withdraw-intent (wallet-intent/make-intent
                                         (deposit-address (:exchange (end edge)) (make-unit (:asset (end edge)) (:method (metadata edge))))
                                         (make-unit (:symbol (metadata edge))
                                                    (:method (metadata edge)))
                                         (withdraw-qty (:base-terms (metadata edge)) (:quote-terms (metadata edge)) qty))
                                        (:exchange (end edge)))))

(defn edge->operation [edge qty]
  (if (order-edge? edge)
    (edge->order edge qty)
    (edge->withdraw edge qty)))

(defn- route->operations [route entry-qty]
  (if (empty? route)
    '()
    (conj (route->operations (rest route) (traverse-edge (first route) entry-qty))
          (edge->operation (first route) entry-qty))))

(defn route->signal [route entry-qty]
  (apply make-signal (conj (route->operations route entry-qty)
                           :seqential)))
