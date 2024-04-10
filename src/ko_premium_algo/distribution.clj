(ns ko-premium-algo.distribution)

(defn distribute [f n seq]
  (reduce concat (map f (partition-all n seq))))
