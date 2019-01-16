(ns om-pm.parser
  (:require [om.next :as om])
  (:refer-clojure :exclude [read]))

(defmulti read om/dispatch)

(defmethod read :default
  [{:keys [state query]} k _]
  (let [st @state]
    {:value (om/db->tree query (k st) st)}))

(defmulti mutate om/dispatch)

(defmethod mutate 'card/move
  [{:keys [state]} _ {:keys [card-id source-column-id destination-column-id]}]
  {:action
   (fn []
     (swap! state update-in
            [:column/by-id (int source-column-id) :column/cards]
            (fn [existing-cards]
              (vec (remove (fn [[_ id]] (= card-id id)) existing-cards))))
     (swap! state update-in
            [:column/by-id (int destination-column-id) :column/cards]
            (fn [existing-cards]
              (conj existing-cards [:card/by-id card-id]))))})

(def parser (om/parser {:read read :mutate mutate}))