(ns ko-premium-algo.graph)

(defn make-edge [weight start end]
  {:weight weight :start start :end end})

(defn nodes [edges]
  (seq
   (reduce (fn [acc edge] (conj acc (:start edge) (:end edge))) #{} edges)))

(defn shortest-info [edges start]
  (let [table (fn [nodes value] (reduce #(conj %1 (hash-map %2 value)) {} nodes))
        nodes (nodes edges)
        distance (assoc (table nodes Double/POSITIVE_INFINITY) start 1)
        parent (table nodes nil)
        relax (fn [distance parent]
                (reduce
                 (fn [[d p] edge]
                   (let [s (:start edge) e (:end edge) w (:weight edge) d-s (get d s) d-e (get d e)]
                     (if (< (* d-s w) d-e)
                       [(assoc d e (* d-s w)) (assoc p e s)]
                       [d p])))
                 [distance parent] edges))]
    (reduce (fn [[d p] _] (relax d p)) [distance parent] (range (count nodes)))))

(defn distance [shortest-info node]
  (get (first shortest-info) node))

(defn way [shortest-info node]
  (let [parent (second shortest-info)]
    (if (nil? (get parent node))
      (list node)
      (concat (way shortest-info (get parent node)) (list node)))))

