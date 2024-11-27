(ns ko-premium-algo.lib.partition)

(defn partition-by-size [total size]
  (if (<= total size)
    (list total)
    (concat (list size) (partition-by-size (- total size) size))))
