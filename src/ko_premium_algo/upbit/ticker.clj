(ns ko-premium-algo.upbit.ticker
  (:require [ko-premium-algo.trade.ticker :refer [make-ticker]]
            [ko-premium-algo.trade.market :refer [symbol]]
            [cheshire.core :as json]
            [clojure.string :as str]
            [clj-http.client :as client]))

(defn ticker [markets]
  (->> (map symbol markets)
       (#(client/get "https://api.upbit.com/v1/ticker" {:query-params {"markets" (str/join "," %)}}))
       (#(json/parse-string (:body %)))
       (map #(make-ticker %1 (get %2 "trade_price")) markets)))

