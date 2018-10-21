(ns aws-dash.parser
  (:require [om.next :as om])
  (:refer-clojure :exclude [read]))

(defmulti read om/dispatch)

(defmethod read :instances
  [{:keys [state query]} k _]
  (let [st @state]
    {:value (:instances st)}))

(defmulti mutate om/dispatch)

(defmethod mutate 'add/instances
  [{:keys [state]} _ {:keys [data]}]
  {:action
   (fn []
     (swap! state update-in
            [:instances]
            (fn [existing-instances]
              (concat existing-instances data))))
   :value {:keys [:instances]}})

(def parser (om/parser {:read read :mutate mutate}))