(ns ko-premium-algo.lib.numeric)

(defn number [n]
  (cond
    (string? n) (number (Double/parseDouble n))
    (number? n) (bigdec n)
    :else (throw (Exception. "Only string or number is allowed"))))

(defn is-number? [n]
  (= (type n) java.math.BigDecimal))
