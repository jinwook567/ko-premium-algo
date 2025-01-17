(ns ko-premium-algo.wallet.unit)

(defn make-unit [asset method]
  {:asset asset :method method})

(defn asset [unit]
  (:asset unit))

(defn method [unit]
  (:method unit))
