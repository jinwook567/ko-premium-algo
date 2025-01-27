(ns ko-premium-algo.strategy.signal)

(defn make-operation [op-type exchange intent]
  {:op-type op-type :exchange exchange :intent intent})

(defn op-type [operation]
  (:op-type operation))

(defn exchange [operation]
  (:exchange operation))

(defn intent [operation]
  (:intent operation))

(defn make-operation-seq [execute-type & operations]
  {:execute-type execute-type :seq operations})

(defn execute-type [operation-seq]
  (:execute-type operation-seq))

(defn operations [operation-seq]
  (:seq operation-seq))
