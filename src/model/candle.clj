(ns model.candle
  (:require [lib.numeric :refer [number]]))

(defn make-candle [low high open close volumn trading-value]
  {:low (number low)
   :high (number high)
   :open (number open)
   :close (number close)
   :volumn (number volumn)
   :trading-value (number trading-value)})

(defn low [candle] (:low candle))

(defn high [candle] (:high candle))

(defn open [candle] (:open candle))

(defn close [candle] (:close candle))

(defn volumn [candle] (:volumn candle))

(defn trading-value [candle] (:trading-value candle))

(def interval #{:1s :1m :5m :30m :1h :1d :1w})

(defn interval->map [interval]
  (let [[_ value unit] (re-matches #"(\d+)([a-zA-Z]+)" (name interval))]
    {:value (Integer. value)
     :unit unit}))

(defn map->interval [interval-map]
  (keyword (str (:value interval-map) (:unit interval-map))))

(defn roc [candle]
  (/ (- (close candle) (open candle)) (open candle)))

;; todo: candle 을 받아서 처리하도록 수정
(defn amihud [roc trading-value]
  (/ roc trading-value))

(defn vwap [prices volumns]
  (/ (reduce + (map #(* %1 %2) prices volumns)) (reduce + volumns)))
