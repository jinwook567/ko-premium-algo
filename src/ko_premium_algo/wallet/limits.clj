(ns ko-premium-algo.wallet.limits)

(defn make-limits [qty-range actions]
  {:qty-range qty-range :actions actions})

(defn qty-range [limits]
  (:qty-range limits))

(defn actions [limits]
  (:actions limits))

(defn can-transfer? [base-actions quote-actions]
  (and (contains? base-actions :withdraw)
       (contains? quote-actions :deposit)))
