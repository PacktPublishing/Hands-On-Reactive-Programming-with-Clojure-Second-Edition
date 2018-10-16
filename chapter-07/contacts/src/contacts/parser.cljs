(ns contacts.parser
  (:require [om.next :as om])
  (:refer-clojure :exclude [read]))

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)

(defn get-contacts [state key]
  (let [st @state]
    (into [] (map #(get-in st %)) (get st key))))

(defmethod read :list/contacts
  [{:keys [state] :as env} key params]
  {:value (get-contacts state key)})

(defmethod mutate 'list/toggle-select-contact
  [{:keys [state]} _ {:keys [name id]}]
  {:action
   (fn []
     (swap! state update-in
            [:contact/by-name name :show-details]
            #(not %)))})

(defmethod mutate 'list/edit-contact
  [{:keys [state]} _ {:keys [name key value]}]
  {:action
   (fn []
     (swap! state update-in
            [:contact/by-name name key] (fn [_] value)))})

(def parser (om/parser {:read read :mutate mutate}))