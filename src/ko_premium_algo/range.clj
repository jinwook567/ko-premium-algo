(ns ko-premium-algo.range
  (:refer-clojure :exclude [min max]))

(defn make-range [min max step]
  {:min min :max max :step step})

(defn min [range]
  (:min range))

(defn max [range]
  (:max range))

(defn step [range]
  (:step range))

(defn satisfy-step? [number step]
  (if (nil? step) 
    true 
    (zero? (mod (/ number step) 1.0))))
