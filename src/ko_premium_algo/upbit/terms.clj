(ns ko-premium-algo.upbit.terms
  (:require [ko-premium-algo.trade.fee :refer [make-fee]]
            [ko-premium-algo.upbit.auth :as auth]
            [clj-http.client :as client]
            [ko-premium-algo.trade.market :as m]
            [ko-premium-algo.trade.terms :refer [make-market-terms make-terms]]
            [ko-premium-algo.trade.limits :refer [make-limits]]
            [ko-premium-algo.lib.range :refer [make-range]]
            [cheshire.core :as json]))

(defn- make-side-terms [response side]
  (make-terms (make-fee :rate (Float/parseFloat (get response (str side "_fee"))))
              (make-limits (make-range 0 Float/POSITIVE_INFINITY 0.00000001)
                           (make-range (Float/parseFloat (get-in response ["market" side "min_total"]))
                                       (Float/parseFloat (get-in response ["market" "max_total"]))
                                       (some->> (get-in response ["market" side "price_unit"])
                                                Float/parseFloat)))))

(defn terms [market]
  (->> (client/get "https://api.upbit.com/v1/orders/chance"
                   {:headers (auth/make-auth-header {:market (m/symbol market)})
                    :query-params {:market (m/symbol market)}})
       (#(json/parse-string (:body %)))
       (#(make-market-terms
          (make-side-terms % "ask")
          (make-side-terms % "bid")))))
