(ns ko-premium-algo.binance.ticker
  (:require [ko-premium-algo.trade.ticker :refer [make-ticker]]
            [ko-premium-algo.trade.market :refer [symbol]]
            [cheshire.core :as json]
            [clojure.string :as str]
            [clj-http.client :as client]))

(defn- ticker-symbols [symbols]
  (str "[" (str/join "," (map #(str "\"" % "\"") symbols)) "]"))

(defn base-ticker [markets]
  (->> (map symbol markets)
       (#(client/get "https://api.binance.com/api/v3/ticker/price" {:query-params {"symbols" (ticker-symbols %)}}))
       (#(json/parse-string (:body %)))
       (map #(make-ticker %1 (Double/parseDouble (get %2 "price"))) markets)))

(defn ticker [markets]
  (flatten (pmap base-ticker (partition-all 400 markets))))
