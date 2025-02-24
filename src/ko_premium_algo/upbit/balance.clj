(ns ko-premium-algo.upbit.balance
  (:require [clj-http.client :as client]
            [ko-premium-algo.upbit.auth :refer [make-auth-header]]
            [cheshire.core :as json]
            [ko-premium-algo.lib.numeric :refer [number]]))

(defn balance [asset]
  (->> (client/get "https://api.upbit.com/v1/accounts"
                   {:headers (make-auth-header)})
       (#(json/parse-string (:body %)))
       (some #(when (= (get % "currency") asset) (get % "balance")))
       number))
