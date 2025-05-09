(ns crypto.exchange.binance.terms
  (:require [clj-http.client :as client]
            [model.market :as m]
            [model.fee :refer [make-fee]]
            [crypto.exchange.binance.auth :as auth]
            [model.range :refer [make-range]]
            [model.terms :refer [make-market-terms make-terms]]
            [model.limits :refer [make-limits]]
            [crypto.exchange.binance.lib :refer [coll->query]]
            [cheshire.core :as json]))

(defn- fee-info []
  (->> (client/get "https://api.binance.com/sapi/v1/asset/tradeFee"
                   {:headers (auth/make-auth-header)
                    :query-params (auth/make-payload)})
       (#(json/parse-string (:body %)))
       (mapcat #(vector (get % "symbol") (get % "makerCommission")))
       (apply hash-map)))

(defn- fee [markets]
  (let [info (fee-info)]
    (map #(make-fee :rate :inclusive (get info (m/symbol %))) markets)))

(defn- make-qty-range [filters]
  (let [qty-filter (some #(when (= (get % "filterType") "LOT_SIZE") %) filters)]
    (make-range (get qty-filter "minQty")
                (get qty-filter "maxQty")
                (get qty-filter "stepSize"))))

(defn- make-price-range [filters]
  (let [price-filter (some #(when (= (get % "filterType") "PRICE_FILTER") %) filters)]
    (make-range (get price-filter "minPrice")
                (get price-filter "maxPrice")
                (get price-filter "tickSize"))))

(defn- make-amount-range [filters]
  (let [amount-filter (some #(when (= (get % "filterType") "NOTIONAL") %) filters)]
    (make-range (get amount-filter "minNotional")
                (get amount-filter "maxNotional")
                nil)))

(defn- limits [markets]
  (->> (client/get "https://api.binance.com/api/v3/exchangeInfo"
                   {:query-params {"symbols" (coll->query (map m/symbol markets))}})
       (#(json/parse-string (:body %)))
       (#(get % "symbols"))
       (map #(get % "filters"))
       (map #(make-limits (make-qty-range %) (make-price-range %) (make-amount-range %)))))

(defn terms [markets]
  (map #(make-market-terms (make-terms %1 %2) (make-terms %1 %2))
       (fee markets)
       (limits markets)))
