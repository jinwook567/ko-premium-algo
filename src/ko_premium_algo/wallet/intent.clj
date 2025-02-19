(ns ko-premium-algo.wallet.intent
  (:require [ko-premium-algo.lib.numeric :refer [number]]))

(defn make-intent [address unit qty]
  {:address address  :unit unit :qty (number qty)})

(defn address [intent]
  (:address intent))

(defn unit [intent]
  (:unit intent))

(defn qty [intent]
  (:qty intent))
