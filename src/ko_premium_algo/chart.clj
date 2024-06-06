(ns ko-premium-algo.chart)

(defn make-candle [low open close high volumn]
  {:low low :open open :close close :high high :volumn volumn})

(defn low [candle] (:low candle))

(defn open [candle] (:open candle))

(defn close [candle] (:close candle))

(defn high [candle] (:high candle))

(defn volumn [candle] (:volumn candle))

(defn make-order-book-item [price qty]
  {:price price :qty qty})

(defn price [order-book-item]
  (:price order-book-item))

(defn qty [order-book-item]
  (:qty order-book-item))

(defn make-trade [price qty time]
  {:price price :qty qty :time time})

(defn trade-price [trade]
  (:price trade))

(defn trade-qty [trade]
  (:qty trade))

(defn trade-time [trade]
  (:time trade))