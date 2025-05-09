(ns model.signal)

(defn make-operation [op-type exchange intent]
  {:op-type op-type :exchange exchange :intent intent})

(defn op-type [operation]
  (:op-type operation))

(defn exchange [operation]
  (:exchange operation))

(defn intent [operation]
  (:intent operation))

(defn make-signal [execute-type & operations]
  {:execute-type execute-type :operations operations})

(defn execute-type [signal]
  (:execute-type signal))

(defn operations [signal]
  (:operations signal))
