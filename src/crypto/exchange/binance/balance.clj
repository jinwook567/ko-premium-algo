(ns crypto.exchange.binance.balance
  (:require [clj-http.client :as client]
            [lib.numeric :refer [number]]
            [cheshire.core :as json]
            [crypto.exchange.binance.auth :as auth]))

(defn balance [asset]
  (->> (client/post "https://api.binance.com/sapi/v3/asset/getUserAsset"
                    {:headers (auth/make-auth-header)
                     :query-params (auth/make-payload {:asset asset})})
       (#(json/parse-string (:body %)))
       (#(get (first %) "free"))
       (#(if (some? %) (number %) 0))))
