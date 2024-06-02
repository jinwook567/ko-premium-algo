(ns ko-premium-algo.market
  (:require [ko-premium-algo.pair :refer [consolidate-pairs]]))

(defn make-market-pair [exchange-rate pair]
  (cons exchange-rate pair))

(defn exchange-rate [market-pair]
  (first market-pair))

(defn pair [market-pair]
  (rest market-pair))

(defn normalize-pair [pair standard-pairs]
  (if (some #(= pair %) standard-pairs)
    (cons 0 pair)
    (cons 1 (reverse pair))))

(defn reversed-pair? [normalized-pair]
  (= (first normalized-pair) 1))

(defn normalize-rate [normalized-pair rate]
  (if (reversed-pair? normalized-pair) (/ 1 rate) rate))

(defn consolidate-market-pairs [linked-market-pairs]
  (make-market-pair
   (reduce #(* %1 (exchange-rate %2)) 1 linked-market-pairs)
   (consolidate-pairs (map pair linked-market-pairs))))

(defn make-exchange-info [min-amount max-amount status fee]
  {:min-amount min-amount :max-amount max-amount :status status :fee fee})

(defn min-amount [exchange-info] (:min-amount exchange-info))

(defn max-amount [exchange-info] (:max-amount exchange-info))

(defn fee [exchange-info] (:fee exchange-info))

(defn can-exchange? [exchange-info] (= (:status exchange-info) "active"))
