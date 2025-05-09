(ns strategy.arbitrage.job.terms
  (:refer-clojure :exclude [min max])
  (:require
   [model.fee :refer [deduct gross]]
   [model.terms :refer [fee limits]]
   [model.limits :refer [qty-range amount-range]]
   [crypto.model.wallet.limits :as wallet-limit :refer [can-transfer? actions]]
   [crypto.model.wallet.terms :as wallet-terms]
   [model.range :refer [min max coerce-limit coerce-step step]]
   [lib.numeric :refer [safe-div]]))

(defn coerce-range [range n]
  (if (> (min range) n)
    0
    (->> (coerce-step (step range) n)
         (coerce-limit (min range) (max range)))))

(defn order-qty [terms price amount]
  (->> amount
       (gross (fee terms))
       (coerce-range (amount-range (limits terms)))
       (#(safe-div % price))
       (coerce-range (qty-range (limits terms)))))

(defn withdraw-qty [base-terms quote-terms qty]
  (->> (if (can-transfer? (actions (wallet-terms/limits base-terms)) (actions (wallet-terms/limits quote-terms))) qty 0)
       (gross (fee base-terms))
       (coerce-range (wallet-limit/qty-range (wallet-terms/limits base-terms)))))

(defn net-qty [terms qty]
  (deduct (fee terms) qty))
