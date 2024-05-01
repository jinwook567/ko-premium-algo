(ns ko-premium-algo.chart)

(defn make-candle [low open close high volumn]
  {:low low :open open :close close :high high :volumn volumn})

(defn low [candle] (:low candle))

(defn open [candle] (:open candle))

(defn close [candle] (:close candle))

(defn high [candle] (:high candle))

(defn volumn [candle] (:volumn candle))