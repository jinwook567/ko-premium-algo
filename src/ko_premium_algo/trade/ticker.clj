(ns ko-premium-algo.trade.ticker)

(defn make-ticker [market price]
  {:market market :price price})

(defn market [ticker]
  (:market ticker))

(defn price [ticker]
  (:price ticker))
