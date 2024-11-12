(ns ko-premium-algo.analyze.kelly-bet)

(defn kelly-bet [success-percent profit-margin loss-margin]
  (let [fail-percent (- 1 success-percent)]
    (- 
     (/ success-percent loss-margin)
     (/ fail-percent profit-margin))))