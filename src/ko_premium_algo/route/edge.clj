(ns ko-premium-algo.route.edge)

(defn make-edge [meta start end]
  {:meta meta :start start :end end})

(defn metadata [edge] (:meta edge))

(defn start [edge] (:start edge))

(defn end [edge] (:end edge))

(defn nodes [edge]
  #{(start edge) (end edge)})
