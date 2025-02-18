(ns ko-premium-algo.chart.analyze)

(defn trading-value [price volumn]
  (* price volumn))

(defn vwap [prices volumns]
  (/ (reduce + (map #(* %1 %2) prices volumns)) (reduce + volumns)))

(defn amihud [roc trading-value]
  (/ roc trading-value))
