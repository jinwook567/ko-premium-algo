(ns ko-premium-algo.binance.terms
  (:require [clj-http.client :as client]
            [ko-premium-algo.trade.market :as m]
            [ko-premium-algo.trade.fee :refer [make-fee]]
            [ko-premium-algo.binance.auth :as auth]
            [ko-premium-algo.lib.range :refer [make-range]]
            [ko-premium-algo.trade.terms :refer [make-market-terms make-terms]]
            [ko-premium-algo.trade.limits :refer [make-limits]]
            [ko-premium-algo.binance.lib :refer [coll->query]]
            [ko-premium-algo.lib.numeric :refer [str->num]]
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
    (map #(make-fee :rate :inclusive (str->num (get info (m/symbol %)))) markets)))

(defn- make-qty-range [filters]
  (let [qty-filter (some #(when (= (get % "filterType") "LOT_SIZE") %) filters)]
    (make-range (str->num (get qty-filter "minQty"))
                (str->num (get qty-filter "maxQty"))
                (str->num (get qty-filter "stepSize")))))

(defn- make-price-range [filters]
  (let [price-filter (some #(when (= (get % "filterType") "PRICE_FILTER") %) filters)]
    (make-range (str->num (get price-filter "minPrice"))
                (str->num (get price-filter "maxPrice"))
                (str->num (get price-filter "tickSize")))))

(defn- make-amount-range [filters]
  (let [amount-filter (some #(when (= (get % "filterType") "NOTIONAL") %) filters)]
    (make-range (str->num (get amount-filter "minNotional"))
                (str->num (get amount-filter "maxNotional"))
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
