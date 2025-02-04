(ns ko-premium-algo.binance.terms
  (:require [clj-http.client :as client]
            [ko-premium-algo.trade.market :as m]
            [ko-premium-algo.trade.fee :refer [make-fee]]
            [ko-premium-algo.binance.auth :as auth]
            [ko-premium-algo.lib.range :refer [make-range]]
            [ko-premium-algo.trade.terms :refer [make-market-terms make-terms]]
            [ko-premium-algo.trade.limits :refer [make-limits]]
            [cheshire.core :as json]))

(defn- fee [market]
  (->> (client/get "https://api.binance.com/sapi/v1/asset/tradeFee"
                   {:headers (auth/make-auth-header)
                    :query-params (auth/make-payload {:symbol (m/symbol market)})})
       (#(first (json/parse-string (:body %))))
       (#(make-fee :rate (Float/parseFloat (get % "makerCommission"))))))


(defn- make-qty-range [filters]
  (let [qty-filter (some #(when (= (get % "filterType") "LOT_SIZE") %) filters)]
    (make-range (Float/parseFloat (get qty-filter "minQty"))
                (Float/parseFloat (get qty-filter "maxQty"))
                (Float/parseFloat (get qty-filter "stepSize")))))

(defn- make-price-range [filters]
  (let [price-filter (some #(when (= (get % "filterType") "PRICE_FILTER") %) filters)]
    (make-range (Float/parseFloat (get price-filter "minPrice"))
                (Float/parseFloat (get price-filter "maxPrice"))
                (Float/parseFloat (get price-filter "tickSize")))))

(defn- make-amount-range [filters]
  (let [amount-filter (some #(when (= (get % "filterType") "NOTIONAL") %) filters)]
    (make-range (Float/parseFloat (get amount-filter "minNotional"))
                (Float/parseFloat (get amount-filter "maxNotional"))
                Float/POSITIVE_INFINITY)))

(defn- limits [market]
  (->> (client/get "https://api.binance.com/api/v3/exchangeInfo?permissions=SPOT"
                   {:query-string {"symbol" (m/symbol market)}})
       (#(json/parse-string (:body %)))
       (#(first (get % "symbols")))
       (#(get % "filters"))
       (#(make-limits (make-qty-range %) (make-price-range %) (make-amount-range %)))))

(defn terms [market]
  (make-market-terms
   (make-terms (fee market) (limits market))
   (make-terms (fee market) (limits market))))
