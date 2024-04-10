(ns ko-premium-algo.market
  (:require [ko-premium-algo.pair :refer [consolidate-pairs]]))

(defn make-market-pair [exchange-rate pair]
  (cons exchange-rate pair))

(defn exchange-rate [market-pair]
  (first market-pair))

(defn pair [market-pair]
  (rest market-pair))

(defn normalize-pairs [pairs standard-pairs]
  (map
   (fn [pair]
     (if (some #(= pair %) standard-pairs)
       (cons 0 pair)
       (cons 1 (reverse pair))))
   pairs))

(defn reversed-pair? [normalized-pair]
  (= (first normalized-pair) 1))

(defn consolidate-market-pairs [linked-market-pairs]
  (make-market-pair
   (reduce #(* %1 (exchange-rate %2)) 1 linked-market-pairs)
   (consolidate-pairs (map pair linked-market-pairs))))

