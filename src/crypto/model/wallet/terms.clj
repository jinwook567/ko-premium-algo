(ns crypto.model.wallet.terms)

(defn make-terms [fee limits]
  {:fee fee :limits limits})

(defn fee [terms]
  (:fee terms))

(defn limits [terms]
  (:limits terms))
