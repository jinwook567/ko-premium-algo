(ns ko-premium-algo.route.graph)

(defn make-edge [meta start end]
  {:meta meta :start start :end end})

(defn metadata [edge] (:meta edge))

(defn start [edge] (:start edge))

(defn end [edge] (:end edge))

(defn nodes [edge]
  #{(start edge) (end edge)})

(defn edges->graph [edges]
  (group-by start edges))

(defn adj [graph node]
  (get graph node []))

(defn eliminate [graph node]
  (dissoc graph node))
