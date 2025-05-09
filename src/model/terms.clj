(ns model.terms)

(defn make-terms [fee limits]
  {:fee fee :limits limits})

(defn fee [terms]
  (:fee terms))

(defn limits [terms]
  (:limits terms))

(defn make-market-terms [ask-terms bid-terms]
  {:ask-terms ask-terms :bid-terms bid-terms})

(defn ask-terms [terms]
  (:ask-terms terms))

(defn bid-terms [terms]
  (:bid-terms terms))
