(ns ko-premium-algo.trade.limits)

(defn make-limits [qty-range amount-range]
  {:qty-range qty-range :amount-range amount-range})

(defn qty-range [limits]
  (:qty-range limits))

(defn amount-range [limits]
  (:amount-range limits))
