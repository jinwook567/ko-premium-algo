(ns ko-premium-algo.graph)

(defn make-edge [weight start end]
  {:weight weight :start start :end end})

(defn nodes [edges]
  (seq
   (reduce (fn [acc edge] (conj acc (:start edge) (:end edge))) #{} edges)))

(defn optimal-exchange-info [edges]
  (let [search (fn [short start end visited]
                 (if (= start end) {:weight 1 :path [start]}
                     (let [connected-edges (filter #(and (= (:end %) end) (not (contains? visited (:start %)))) edges)]
                       (if (empty? connected-edges)
                         {:weight Double/POSITIVE_INFINITY :path []}
                         (let [connect-edge-from-start (fn [edge]
                                                         (let [short-from-start (short start (:start edge) (conj visited (:start edge)))]
                                                           {:weight (* (:weight short-from-start) (:weight edge)) :path (conj (:path short-from-start) (:end edge))}))]
                           (reduce #(if (< (:weight %1) (:weight %2)) %1 %2) (map connect-edge-from-start connected-edges)))))))
        memorized-search (memoize search)
        short (fn short [start end visited] (memorized-search short start end visited))]
    (memoize (fn [start end] (search short start end #{end})))))

(defn exchange-rate [optimal-exchange-info]
  (:weight optimal-exchange-info))

(defn path [optimal-exchange-info]
  (:path optimal-exchange-info))