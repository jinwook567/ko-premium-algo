(ns crypto.exchange.upbit.balance
  (:require [clj-http.client :as client]
            [crypto.exchange.upbit.auth :refer [make-auth-header]]
            [cheshire.core :as json]
            [lib.numeric :refer [number]]))

(defn balance [asset]
  (->> (client/get "https://api.upbit.com/v1/accounts"
                   {:headers (make-auth-header)})
       (#(json/parse-string (:body %)))
       (some #(when (= (get % "currency") asset) (get % "balance")))
       (#(if (some? %) (number %) 0))))
