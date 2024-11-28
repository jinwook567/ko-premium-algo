(ns ko-premium-algo.chart.candle)

(defn make-candle [low high open close volumn]
  {:low low :high high :open open :close close :volumn volumn})

(defn low [candle] (:low candle))

(defn high [candle] (:high candle))

(defn open [candle] (:open candle))

(defn close [candle] (:close candle))

(defn volumn [candle] (:volumn candle))

(def interval #{:1s :1m :5m :30m :1h :1d :1w})
