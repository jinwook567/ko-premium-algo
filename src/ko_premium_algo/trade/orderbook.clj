(ns ko-premium-algo.trade.orderbook
  (:require [ko-premium-algo.trade.intent :refer [side]]))

(defn make-orderbook [intents]
  (let [group (group-by side intents)]
    {:asks (group :ask) :bids (group :bid)}))
