(ns ko-premium-algo.trade.limits)

(defn make-limits [qty-range price-range amount-range]
  {:qty-range qty-range :price-range price-range :amount-range amount-range})

(defn qty-range [limits]
  (:qty-range limits))

(defn price-range [limits]
  (:price-range limits))

(defn amount-range [limits]
  (:amount-range limits))
