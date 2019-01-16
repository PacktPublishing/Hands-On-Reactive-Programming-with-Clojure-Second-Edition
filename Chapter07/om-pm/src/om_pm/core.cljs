(ns om-pm.core
  (:require [goog.dom :as gdom]
            [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [cljs.core.async :refer [put! chan <!]]
            [om-pm.parser :as parser]
            [om-pm.util :refer [set-transfer-data! get-transfer-data!]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def init-data
  {:card/by-id {1 {:db/id 1 :card/name "Expenses" :card/description "Submit last client's expense report"}
                2 {:db/id 2 :card/name "Groceries shopping" :card/description "Almond milk, mixed nuts, eggs..."}}
   :column/list     [[:column/by-id 1]
                     [:column/by-id 2]
                     [:column/by-id 3]]
   :column/by-id {1 {:db/id 1
                     :column/title "Backlog"
                     :column/cards [[:card/by-id 1] [:card/by-id 2]]}
                  2 {:db/id 2
                     :column/title "In Progress"
                     :column/cards []}
                  3 {:db/id 3
                     :column/title "Done"
                     :column/cards []}}})

(defn- handle-drop [e transfer-chan destination-column-id]
  (.preventDefault e)
  (let [data {:card-id (js/parseInt (get-transfer-data! e "cardId"))
              :source-column-id (get-transfer-data! e "sourceColumnId")
              :destination-column-id destination-column-id}]
    (put! transfer-chan data)))

(defn- card-view [{:keys [db/id card/name card/description]} column-id]
  (dom/div #js {:key id
                :className   "card"
                :draggable   true
                :onDragStart (fn [e]
                               (set-transfer-data! e "cardId" id)
                               (set-transfer-data! e "sourceColumnId" column-id))}
           (dom/div nil name)
           (dom/div nil description)))

(defui Column
       static om/Ident
       (ident [this {:keys [db/id]}]
              [:column/by-id id])
       static om/IQuery
       (query [this]
              [:db/id :column/title {:column/cards [:db/id :card/name :card/description]}])
       Object
       (render [this]
               (let [{:keys [db/id column/title column/cards transfer-chan] :as data} (om/props this)]
                 (dom/div #js {:className "column"
                               :onDragOver #(.preventDefault %)
                               :onDrop #(handle-drop % transfer-chan id)}
                          (dom/div #js {:className "column-title"} title)
                          (if cards
                            (map #(card-view % id) cards))))))

(def column* (om/factory Column ))

(defn column [props transfer-chan]
  (column* (assoc props :transfer-chan transfer-chan)))

(defn column-view [columns transfer-chan]
  (apply dom/div nil
         (map #(column % transfer-chan) columns)))

(defui RootView
       static om/IQuery
       (query [this]
              [{:column/list (om/get-query Column)}])
       Object
       (initLocalState [_]
                       {:transfer-chan (chan)})
       (componentDidMount [this nextprops nextstate]
                          (let [transfer-chan (om/get-state this :transfer-chan)]
                            (go-loop []
                                     (let [transfer-data (<! transfer-chan)]
                                       (om/transact! this `[(card/move ~transfer-data)])
                                       (recur)))))
       (render [this]
               (let [{:keys [:column/list]} (om/props this)
                     {:keys [transfer-chan]} (om/get-state this)]
                 (dom/div nil
                          (column-view list transfer-chan)))))

(def reconciler
  (om/reconciler
    {:state     (atom init-data)
     :parser    parser/parser}))

(defn start []
  (om/add-root!
    reconciler
    RootView
    (gdom/getElement "app")))

(start)

(defn on-js-reload []
  (start))