(ns ko-premium-algo.wallet.transfer)

(defn make-transfer [id txid side intent created-at state]
  {:id id :txid txid :side side :intent intent :created-at created-at :state state})

(defn id [transfer]
  (:id transfer))

(defn txid [transfer]
  (:txid transfer))

(defn side [transfer]
  (:side transfer))

(defn intent [transfer]
  (:intent transfer))

(defn created-at [transfer]
  (:created-at transfer))

(defn state [transfer]
  (:state transfer))
