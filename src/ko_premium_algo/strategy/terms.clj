(ns ko-premium-algo.strategy.terms
  (:refer-clojure :exclude [min max])
  (:require
   [ko-premium-algo.trade.fee :refer [coerce-fee]]
   [ko-premium-algo.trade.terms :refer [fee limits]]
   [ko-premium-algo.trade.limits :refer [qty-range amount-range]]
   [ko-premium-algo.wallet.limits :as wallet-limit]
   [ko-premium-algo.lib.range :refer [min max coerce-limit coerce-step step]]))

(defn coerce-range [range n]
  (if (> (min range) n)
    0
    (->> (coerce-step (step range) n)
         (coerce-limit (min range) (max range)))))

(defn order-qty [terms price amount]
  (->> amount
       (coerce-fee (fee terms))
       (coerce-range (amount-range (limits terms)))
       (#(/ % price))
       (coerce-range (qty-range (limits terms)))))

(defn withdraw-qty [terms qty]
  (->> qty
       (coerce-fee (fee terms))
       (coerce-range (wallet-limit/qty-range (limits terms)))))
