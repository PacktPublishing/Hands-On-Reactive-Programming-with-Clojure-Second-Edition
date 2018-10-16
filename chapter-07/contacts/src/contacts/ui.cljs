(ns contacts.ui
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defn set-value [component name key]
  (fn [e]
    (om/transact! component `[(list/edit-contact ~{:name name :key key :value (.. e -target -value)})])))

(defui Contact
       static om/Ident
       (ident [this {:keys [name]}]
              [:contact/by-name name])
       static om/IQuery
       (query [this]
              '[:name :phone :email])
       Object
       (render [this]
               (let [{:keys [phone name email show-details] :as props} (om/props this)]
                 (dom/li nil
                         (dom/label nil (str name " (" phone ") (" email ")"))
                         (dom/button
                           #js {:style #js {:marginLeft "10px"}
                                :onClick
                                       (fn [e]
                                         (om/transact! this
                                                       `[(list/toggle-select-contact ~props)]))}
                           "Show details")
                         (if show-details
                           (dom/div nil
                                    (dom/div #js {:className "input-div"}
                                             (dom/label #js {:className "label"} "Email")
                                             (dom/input #js {:value email :onChange (set-value this name :email)}))
                                    (dom/div #js {:className "input-div"}
                                             (dom/label #js {:className "label"}  "Phone")
                                             (dom/input #js {:value phone :onChange (set-value this name :phone)}))))))))

(def contact (om/factory Contact {:keyfn :name}))

(defui ListView
       Object
       (render [this]
               (let [list (om/props this)]
                 (apply dom/ul nil
                        (map contact list)))))

(def list-view (om/factory ListView))

(defui RootView
       static om/IQuery
       (query [this]
              (let [subquery (om/get-query Contact)]
                `[{:list/contacts ~subquery}]))
       Object
       (render [this]
               (let [{:keys [list/contacts]} (om/props this)]
                 (dom/div nil
                          (apply dom/div nil
                                 [(dom/h2 nil "Contacts")
                                  (list-view contacts)])))))