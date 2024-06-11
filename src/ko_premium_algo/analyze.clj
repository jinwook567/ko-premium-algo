(ns ko-premium-algo.analyze)

(defn interquartile-range [numbers]
  (let [sorted-numbers (vec (sort numbers))
        len (count sorted-numbers)
        Q1 (nth sorted-numbers (Math/floor (/ len 4)))
        Q3 (nth sorted-numbers (Math/floor (* 3 (/ len 4))))
        IQR (- Q3 Q1)
        LIF (- Q1 (* 1.5 IQR))
        UIF (+ Q3 (* 1.5 IQR))]
    (fn [arg]
      (and (>= arg LIF) (<= arg UIF)))))

(defn kelly-bet [success-percent profit-margin loss-margin]
  (let [fail-percent (- 1 success-percent)]
    (- 
     (/ success-percent loss-margin)
     (/ fail-percent profit-margin))))