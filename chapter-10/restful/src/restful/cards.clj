(ns restful.cards
  (:require [compojure.api.sweet :refer [GET POST PUT DELETE]]
            [restful.models.card :refer [Card]]
            [restful.util :as str]
            [ring.util.http-response :refer [ok not-found created]]
            [schema.core :as s]
            [toucan.db :as db]))

(defn valid-name? [name]
  (str/non-blank-with-max-length? 50 name))

(defn valid-description? [description]
  (str/length-in-range? 5 100 description))

(s/defschema CardRequestSchema
             {:name (s/constrained s/Str valid-name?)
              :description (s/constrained s/Str valid-description?)})

(defn id->created [id]
  (created (str "/cards/" id) {:id id}))

(defn create-card-handler [create-card-req]
  (->> create-card-req
       (db/insert! Card)
       :id
       id->created))

(defn card->response [card]
  (if card
    (ok card)
    (not-found)))

(defn get-card-handler [card-id]
  (card->response (Card card-id)))

(defn get-cards-handler []
  (ok (db/select Card)))

(defn update-card-handler [id update-card-req]
  (db/update! Card id update-card-req)
  (ok))

(defn delete-card-handler [card-id]
  (db/delete! Card :id card-id)
  (ok))

(def card-routes
  [(POST "/cards" []
         :body [create-card-req CardRequestSchema]
         (create-card-handler create-card-req))
   (GET "/cards/:id" []
        :path-params [id :- s/Int]
        (get-card-handler id))
   (GET "/cards" []
        (get-cards-handler))
   (PUT "/cards/:id" []
        :path-params [id :- s/Int]
        :body [update-card-req CardRequestSchema]
        (update-card-handler id update-card-req))
   (DELETE "/cards/:id" []
           :path-params [id :- s/Int]
           (delete-card-handler id))])