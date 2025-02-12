(ns ko-premium-algo.strategy.execute
  (:require [ko-premium-algo.lib.async :refer [sequential concurrent poll-until]]
            [ko-premium-algo.strategy.signal :refer [op-type exchange intent execute-type operations]]
            [ko-premium-algo.gateway.order :refer [execute-order order]]
            [ko-premium-algo.gateway.transfer :refer [execute-withdraw transfer]]
            [ko-premium-algo.trade.order :as ord]
            [ko-premium-algo.trade.intent :refer [market]]
            [ko-premium-algo.lib.time :refer [make-duration]]
            [ko-premium-algo.wallet.transfer :as trans]))

(defmulti execute-operation op-type)

(defmethod execute-operation :order [operation]
  (let [open-order (execute-order (exchange operation) (intent operation))
        fetch-order #(order (exchange operation) (market (ord/intent open-order)) (ord/id open-order))
        order-done? #(= (ord/state %) :done)]
    (poll-until fetch-order order-done? (make-duration 1 "s"))))

(defmethod execute-operation :withdraw [operation]
  (let [withdraw (execute-withdraw (exchange operation) (intent operation))
        fetch-withdraw #(transfer (exchange operation) (trans/side withdraw) (trans/txid withdraw))
        withdraw-done? #(= (trans/state %) "DONE")]
    (poll-until fetch-withdraw withdraw-done? (make-duration 1 "s"))))

(defn- is-signal? [signal]
  (and (some? (execute-type signal))
       (some? (operations signal))))

(defn execute-signal [signal]
  (->> (operations signal)
       (map #(if (is-signal? %)
               (partial execute-signal %)
               (partial execute-operation %)))
       (apply (if (= (execute-type signal) :seqential)
                sequential
                concurrent))))
