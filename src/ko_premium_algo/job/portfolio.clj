(ns ko-premium-algo.job.portfolio)

(defn pnl-rate [signal]
  (* 100 (/ (- (:return-qty signal) (:entry-qty signal)) (:entry-qty signal))))

(defn approve? [signal]
  (< 1 (pnl-rate signal)))
