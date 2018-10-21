(ns aws-dash.core
    (:require [aws-dash.observables :as obs]
              [aws-dash.parser :as parser]
              [om.dom :as dom]
              [om.next :as om :refer-macros [defui]]))

(enable-console-print!)

(defonce app-state (atom {:instances []}))

(def resources (obs/stack-resources))

(defui Column
       static om/IQuery
       (query [this]
              [:instance-id :type :status])
       Object
       (render [this]
               (let [{:keys [instance-id type status]} (om/props this)]
                 (dom/tr #js {:key instance-id}
                         (dom/td nil instance-id)
                         (dom/td nil type)
                         (dom/td nil status)))))

(def column (om/factory Column {:keyfn :instance-id}))

(defn column-view [columns]
  (map column columns))

(defui RootView
       static om/IQuery
       (query [this]
              '[:instances])
       Object
       (render [this]
               (let [{:keys [:instances]} (om/props this)]
                 (dom/div nil
                          (dom/h1 nil "Stack Resource Statuses")
                          (dom/table #js {:style #js {:border "1px solid black"}}
                                     (dom/tbody nil
                                                (dom/tr nil
                                                        (dom/th nil "Id")
                                                        (dom/th nil "Type")
                                                        (dom/th nil "Status"))
                                                (column-view instances)))))))

(def reconciler
  (om/reconciler
    {:state     app-state
     :parser    parser/parser}))

(defn start []
  (om/add-root!
    reconciler
    RootView
    (.getElementById js/document "app")))

(start)

(.subscribe (-> (obs/rx-merge (obs/rds-instance-status resources)
                              (obs/ec2-instance-status resources))
                (.pipe (obs/rx-reduce conj [])))
            (fn [data]
              (let [pass {:data (filter (fn [m]
                                          (map? m))
                                        data)}]
                (om/transact! reconciler `[(add/instances ~pass)]))))

(defn on-js-reload []
  (start))