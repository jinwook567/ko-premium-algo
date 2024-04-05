(ns ko-premium-algo.pair)

(defn make-pair [base quote]
  (list base quote))

(defn base [pair]
  (first pair))

(defn quote [pair]
  (second pair))

(defn linked-pairs? [pairs]
  (cond
    (empty? pairs) false
    (empty? (rest pairs)) true
    :else (and (= (ko-premium-algo.pair/quote (first pairs)) (base (second pairs))) (linked-pairs? (rest pairs)))))

(defn consolidate-pairs [linked-pairs]
  (make-pair (base (first linked-pairs)) (ko-premium-algo.pair/quote (last linked-pairs))))