(ns contacts.core
  (:require [om.next :as om]
            [contacts.parser :as parser]
            [contacts.ui :as ui]))

(enable-console-print!)

(def init-data
  {:list/contacts [{:name  "James Hetfield"
                    :email "james@metallica.com"
                    :phone "+1 XXX XXX XXX"}
                   {:name  "Adam Darski"
                    :email "the.nergal@behemoth.pl"
                    :phone "+48 XXX XXX XXX"}]})

(def reconciler
  (om/reconciler
    {:state init-data
     :parser parser/parser}))

(defn mount-root-view! []
  (om/add-root! reconciler ui/RootView (.getElementById js/document "app")))

(mount-root-view!)

(defn on-js-reload []
  (mount-root-view!))