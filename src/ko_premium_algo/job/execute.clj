(ns ko-premium-algo.job.execute
  (:require [ko-premium-algo.trade.intent :refer [make-intent market]]
            [ko-premium-algo.trade.market :refer [make-market]]
            [ko-premium-algo.route.edge :refer [start end metadata]]
            [ko-premium-algo.wallet.intent :as wallet-intent]
            [ko-premium-algo.wallet.unit :refer [make-unit]]
            [ko-premium-algo.trade.order :refer [id intent state]]
            [ko-premium-algo.gateway.order :refer [order execute-order]]
            [ko-premium-algo.gateway.transfer :refer [execute-withdraw transfer]]
            [ko-premium-algo.wallet.transfer :as transfer]
            [clojure.core.async :refer [chan go-loop >! <! timeout]]
            [ko-premium-algo.job.weight :refer [edge-weight]]))

(defn- trade-edge? [edge]
  (not= (:type (:meta edge)) :withdraw))

(defn- bid-edge? [edge]
  (= (:type (:meta edge)) :bid))

(defn- ask-edge->intent [edge qty]
  (make-intent (make-market (:asset (end edge))
                            (:asset (start edge))
                            (:symbol (:meta edge)))
               (:type (metadata edge))
               qty
               (/ 1 (:price (metadata edge)))))

(defn- bid-edge->intent [edge qty]
  (make-intent (make-market (:asset (start edge))
                            (:asset (end edge))
                            (:symbol (metadata edge)))
               (:type (metadata edge))
               (/ qty (:price (metadata edge)))
               (:price (metadata edge))))

(defn- edge->order-intent [edge qty]
  ((if (bid-edge? edge)
     bid-edge->intent
     ask-edge->intent) edge qty))

(defn- edge->transfer-intent [edge qty]
  (wallet-intent/make-intent "end exchange address"
                             (make-unit (:symbol (metadata edge))
                                        (:method (metadata edge)))
                             qty))

(defn- done-order? [exchange order-response]
  (= (state (order exchange (market (intent order-response)) (id order-response))) :done))

(defn- done-transfer? [exchange transfer-response]
  (= (transfer/state (transfer exchange (transfer/side transfer-response) (transfer/txid transfer-response))) "DONE"))

(defn- execute-edge [edge qty execute check]
  (let [response (execute edge qty)
        done-chan (chan 1)]
    (go-loop []
      (if (check response)
        (>! done-chan response)
        (do
          (println "retry check" response)
          (<! (timeout 1000))
          (recur))))))

(defn- execute-trade [edge qty]
  (execute-order (:exchange (start edge)) (edge->order-intent edge qty)))

(defn- execute-transfer [edge qty]
  (execute-withdraw (:exchange (start edge)) (edge->transfer-intent edge qty)))

(defn- executer [edge]
  (if (trade-edge? edge)
    execute-trade
    execute-transfer))

(defn- checker [edge]
  (let [exchange (:exchange (start edge))]
    (if (trade-edge? edge)
      (partial done-order? exchange)
      (partial done-transfer? exchange))))

(defn execute [route qty]
  (go-loop [edges route
            qty qty]
    (when-let [edge (first edges)]
      (<! (execute-edge edge qty (executer edge) (checker edge)))
      (recur (rest edges) ((edge-weight edge) qty)))))
