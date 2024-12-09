(ns ko-premium-algo.wallet.intent)

(defn make-intent [address method asset qty]
  {:address address  :method method :asset asset :qty qty})

(defn address [intent]
  (:address intent))

(defn method [intent]
  (:method intent))

(defn asset [intent]
  (:asset intent))

(defn qty [intent]
  (:qty intent))
