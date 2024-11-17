(ns ko-premium-algo.trade.intent)

(defn make-intent [market side qty price]
  {:market market :qty qty :price price :side side})

(defn market [intent]
  (:market intent))

(defn qty [intent]
  (:qty intent))

(defn price [intent]
  (:price intent))

(defn side [intent]
  (:side intent))

(defn amount [intent]
  (* (price intent) (qty intent)))