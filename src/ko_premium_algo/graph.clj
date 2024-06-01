(ns ko-premium-algo.graph)

(defn make-edge [weight start end]
  {:weight weight :start start :end end})

(defn weight [edge] (:weight edge))

(defn start [edge] (:start edge))

(defn end [edge] (:end edge))

(defn optimal-route [edges, choice]
  (let [search (fn [short start-node end-node visited]
                 (let [destination-edges (filter #(and (= end-node (end %)) (not (contains? visited (start %)))) edges)
                       shortest-route-to-node (fn [node] (short start-node node (conj visited node)))
                       shortest-route-to-edge (fn [edge]
                                                 (if (= start-node (start edge)) 
                                                   [edge]
                                                   (some-> (shortest-route-to-node (start edge)) (conj edge))))]
                   (choice (map shortest-route-to-edge destination-edges))))
        memorized-search (memoize search)
        short (fn short [start end visited] (memorized-search short start end visited))]
    (memoize (fn [start end] (search short start end #{end})))))