(ns crypto.exchange.binance.lib
  (:require [clojure.string :refer [join]]))

(defn coll->query [coll]
  (str "[" (join "," (map #(str "\"" % "\"") coll)) "]"))
