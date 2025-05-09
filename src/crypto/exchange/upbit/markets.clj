(ns crypto.exchange.upbit.markets
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [clojure.string :as str]
            [model.market :refer [make-market]]))

(defn- assets [symbol]
  {:base (first (str/split symbol #"-"))
   :quote (second (str/split symbol #"-"))})

(defn markets []
  (->> (:body (client/get "https://api.upbit.com/v1/market/all"))
       json/parse-string
       (map #(let [symbol (get % "market")]
               (make-market (:base (assets symbol)) (:quote (assets symbol)) symbol)))))