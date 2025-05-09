(ns model.orderbook
  (:require [model.intent :refer [side]]))

(defn make-orderbook [intents]
  (let [group (group-by side intents)]
    {:asks (group :ask) :bids (group :bid)}))
