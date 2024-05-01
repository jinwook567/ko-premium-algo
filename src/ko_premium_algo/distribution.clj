(ns ko-premium-algo.distribution)

(defn distribute [f n seq]
  (reduce concat (map f (partition-all n seq))))

(defn distribute-number [n num]
  (if (>= n num)
    (list num)
    (cons n (distribute-number n (- num n)))))