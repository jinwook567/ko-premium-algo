(ns ko-premium-algo.lib.numeric)

(defn number [n]
  (cond
    (string? n) (number (Double/parseDouble n))
    (Double/isInfinite n) n
    (number? n) (bigdec n)
    :else (throw (Exception. "Only string or number is allowed"))))

(defn is-number? [n]
  (= (type n) java.math.BigDecimal))

(defn- div
  ([x y scale] (.stripTrailingZeros (.divide x y scale java.math.RoundingMode/FLOOR)))
  ([x y] (div x y 20)))

(defn safe-div
  ([x y scale] (if (or (is-number? x) (is-number? y))
                 (div (number x) (number y) scale)
                 (/ x y)))
  ([x y] (safe-div x y 20)))
