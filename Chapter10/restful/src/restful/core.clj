(ns restful.core
  (:require [toucan.db :as db]
            [toucan.models :as models]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.api.sweet :refer [api routes]]
            [restful.cards :refer [card-routes]])
  (:gen-class))

(def db-spec
  {:dbtype "postgres"
   :dbname "restful"})

(def swagger-conf
  {:ui "/swagger"
   :spec "/swagger.json"
   :options {:ui {:validatorUrl nil}
             :data {:info {:version "1.0.0", :title "Restful Microservice CRUD API"}}}})

(def app (api {:swagger swagger-conf} (apply routes card-routes)))

(defn -main [& args]
  (db/set-default-db-connection! db-spec)
  (models/set-root-namespace! 'resultful.models)
  (run-jetty app {:port 3000}))