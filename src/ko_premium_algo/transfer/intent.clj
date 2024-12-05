(ns ko-premium-algo.transfer.intent)

(defn make-intent [address side method asset qty]
  {:address address :side side :method method :asset asset :qty qty})

(defn address [intent]
  (:address intent))

(defn side [intent]
  (:side intent))

(defn method [intent]
  (:method intent))

(defn asset [intent]
  (:asset intent))

(defn qty [intent]
  (:qty intent))
