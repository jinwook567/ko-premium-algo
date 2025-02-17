(ns ko-premium-algo.strategy.terms
  (:refer-clojure :exclude [min max])
  (:require
   [ko-premium-algo.trade.fee :refer [deduct gross]]
   [ko-premium-algo.trade.terms :refer [fee limits]]
   [ko-premium-algo.trade.limits :refer [qty-range amount-range]]
   [ko-premium-algo.wallet.limits :as wallet-limit :refer [can-transfer? actions]]
   [ko-premium-algo.wallet.terms :as wallet-terms]
   [ko-premium-algo.lib.range :refer [min max coerce-limit coerce-step step]]))

(defn coerce-range [range n]
  (if (> (min range) n)
    0
    (->> (coerce-step (step range) n)
         (coerce-limit (min range) (max range)))))

(defn order-qty [terms price amount]
  (->> amount
       (gross (fee terms))
       (coerce-range (amount-range (limits terms)))
       (#(/ % price))
       (coerce-range (qty-range (limits terms)))))

(defn withdraw-qty [base-terms quote-terms qty]
  (->> (if (can-transfer? (actions (wallet-terms/limits base-terms)) (actions (wallet-terms/limits quote-terms))) qty 0)
       (gross (fee base-terms))
       (coerce-range (wallet-limit/qty-range (wallet-terms/limits base-terms)))))

(defn net-qty [terms qty]
  (deduct (fee terms) qty))
