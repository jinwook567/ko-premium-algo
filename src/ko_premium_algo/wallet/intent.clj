(ns ko-premium-algo.wallet.intent)

(defn make-intent [address unit qty]
  {:address address  :unit unit :qty qty})

(defn address [intent]
  (:address intent))

(defn unit [intent]
  (:unit intent))

(defn qty [intent]
  (:qty intent))
