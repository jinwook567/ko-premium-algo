(ns ko-premium-algo.lib.range
  (:refer-clojure :exclude [min max]))

(defn make-range [min max step]
  {:min min :max max :step step})

(defn min [range]
  (:min range))

(defn max [range]
  (:max range))

(defn step [range]
  (:step range))

(defn precise [op]
  (fn [& numbers]
    (double (apply op (map bigdec numbers)))))

(defn satisfy-step? [step n]
  (or (nil? step) (zero? ((precise mod) n step))))

(defn satisfy-limit? [min max n]
  (and (>= n min)
       (<= n max)))

(defn satisfy? [range n]
  (and (satisfy-step? (step range) n)
       (satisfy-limit? (min range) (max range) n)))

(defn coerce-limit [min max n]
  (cond
    (< n min) min
    (> n max) max
    :else n))

(defn coerce-step
  ([step n] (coerce-step step n true))
  ([step n floor?] (cond
                     (satisfy-step? step n) n
                     floor? ((precise -) n ((precise mod) n step))
                     :else ((precise +) (coerce-step step n) step))))

(defn decimal-step [n]
  (apply / (cons 1 (map (fn [_] 10) (range n)))))
