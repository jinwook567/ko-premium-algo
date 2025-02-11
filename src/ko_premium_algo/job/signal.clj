(ns ko-premium-algo.job.signal
  (:require [ko-premium-algo.strategy.signal :refer [make-operation make-signal]]
            [ko-premium-algo.route.graph :refer [start end metadata]]
            [ko-premium-algo.trade.intent :refer [make-intent]]
            [ko-premium-algo.trade.market :refer [make-market]]
            [ko-premium-algo.wallet.intent :as wallet-intent]
            [ko-premium-algo.wallet.unit :refer [make-unit]]
            [ko-premium-algo.strategy.terms :refer [order-qty withdraw-qty]]
            [ko-premium-algo.job.traverse :refer [traverse-edge]]
            [ko-premium-algo.lib.numeric :refer [precise]]
            [ko-premium-algo.gateway.transfer :refer [deposit-address]]))

(defn- order-edge? [edge]
  (let [type (:type (metadata edge))]
    (or (= type :ask) (= type :bid))))

(defn- ask-edge->intent [edge qty]
  (make-intent (make-market (:asset (end edge))
                            (:asset (start edge))
                            (:symbol (metadata edge)))
               (:type (metadata edge))
               (order-qty (:terms (metadata edge)) (:price (metadata edge)) ((precise *) (:price (metadata edge)) qty))
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
                  (wallet-intent/make-intent
                   (deposit-address (:exchange (end edge)) (make-unit (:asset (end edge)) (:method (metadata edge))))
                   (make-unit (:symbol (metadata edge))
                              (:method (metadata edge)))
                   (withdraw-qty (:base-terms (metadata edge)) (:quote-terms (metadata edge)) qty))))

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
