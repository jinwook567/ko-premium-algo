(ns ko-premium-algo.lib.numeric)

(defn str->num [str]
  (Double/parseDouble str))

(defn precise [op]
  (fn [& numbers]
    (double (apply op (map bigdec numbers)))))