(ns ko-premium-algo.route.edge)

(defn make-edge [weight start end]
  {:weight weight :start start :end end})

(defn weight [edge] (:weight edge))

(defn start [edge] (:start edge))

(defn end [edge] (:end edge))

(defn nodes [edge]
  #{(start edge) (end edge)})
