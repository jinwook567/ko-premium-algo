(ns ko-premium-algo.chart.candle)

(defn make-candle [low high open close volumn]
  {:low low :high high :open open :close close :volumn volumn})

(defn low [candle] (:low candle))

(defn high [candle] (:high candle))

(defn open [candle] (:open candle))

(defn close [candle] (:close candle))

(defn volumn [candle] (:volumn candle))