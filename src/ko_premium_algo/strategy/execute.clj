(ns ko-premium-algo.strategy.execute
  (:require [ko-premium-algo.lib.async :refer [sequential concurrent poll-until]]
            [ko-premium-algo.strategy.signal :refer [op-type exchange intent execute-type  operations]]
            [ko-premium-algo.gateway.order :refer [execute-order order]]
            [ko-premium-algo.gateway.transfer :refer [execute-withdraw transfer]]
            [ko-premium-algo.trade.order :refer [id intent state]]
            [ko-premium-algo.trade.intent :refer [market]]
            [ko-premium-algo.lib.time :refer [make-duration]]
            [ko-premium-algo.wallet.transfer :as trans]))

(defmulti execute-operation op-type)

(defmethod execute-operation :order [operation]
  (let [open-order (execute-order (exchange operation) (intent operation))
        fetch-order #(order (exchange operation) (market (intent open-order)) (id open-order))
        order-done? #(= (state %) :done)]
    (poll-until fetch-order order-done? (make-duration 1 "s"))))

(defmethod execute-operation :withdraw [operation]
  (let [withdraw (execute-withdraw (exchange operation) (intent operation))
        fetch-withdraw #(transfer exchange (trans/side withdraw) (trans/txid withdraw))
        withdraw-done? #(= (trans/state %) "DONE")]
    (poll-until fetch-withdraw withdraw-done? (make-duration 1 "s"))))

(defn- is-operation-seq? [operation-seq]
  (and (some? (execute-type operation-seq))
       (some? (operations operation-seq))))

(defn execute-signal [signal]
  (->> (operations signal)
       (map #(if (is-operation-seq? %)
               (fn [op-seq] (execute-signal op-seq))
               (fn [op] (execute-operation op))))
       (apply (if (= (execute-type signal) :seqential)
                sequential
                concurrent))))
