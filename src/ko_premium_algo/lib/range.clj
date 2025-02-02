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

(defn satisfy-step? [n step]
  (or (nil? step) (zero? (mod n step))))

(defn satisfy-limit? [n min max]
  (and (>= n min)
       (<= n max)))

(defn satisfy? [n range]
  (and (satisfy-step? n (step range))
       (satisfy-limit? n (min range) (max range))))