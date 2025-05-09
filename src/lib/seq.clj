(ns lib.seq)

(defn partition-by-size [total size]
  (if (<= total size)
    (list total)
    (concat (list size) (partition-by-size (- total size) size))))

(defn cartesian-product
  ([list] list)
  ([list1 list2] (for [x list1 y list2] (list x y)))
  ([list1 list2 & more] (reduce #(map flatten (cartesian-product %1 %2)) list1 (conj more list2))))

(defn percent-idx [percent seq]
  (->> (count seq)
       (* (/ percent 100))
       int
       dec
       (max 0)))
