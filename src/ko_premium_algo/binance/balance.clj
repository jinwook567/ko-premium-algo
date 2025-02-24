(ns ko-premium-algo.binance.balance
  (:require [clj-http.client :as client]
            [ko-premium-algo.lib.numeric :refer [number]]
            [cheshire.core :as json]
            [ko-premium-algo.binance.auth :as auth]))

(defn balance [asset]
  (->> (client/post "https://api.binance.com/sapi/v3/asset/getUserAsset"
                    {:headers (auth/make-auth-header)
                     :query-params (auth/make-payload {:asset asset})})
       (#(json/parse-string (:body %)))
       (#(get (first %) "free"))
       number))

(balance "USDT")