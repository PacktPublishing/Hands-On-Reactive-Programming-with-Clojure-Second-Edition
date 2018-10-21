(ns aws-api-stub.core.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [aws-api-stub.aws :as aws]
            [cheshire.core :refer [generate-string]]))


(defn allow-cross-origin
  "middleware function to allow cross origin"
  [handler]
  (fn [request]
   (let [response (handler request)]
    (assoc-in response [:headers "Access-Control-Allow-Origin"]
         "*"))))

(defroutes app-routes
  (GET "/cloudFormation/describeStacks" [] (generate-string (aws/describe-stacks)))
  (GET "/cloudFormation/describeStackResources" [] (generate-string (aws/describe-stack-resources)))
  (GET "/ec2/describeInstances" [] (generate-string (aws/describe-instances)))
  (GET "/rds/describeDBInstances" [] (generate-string (aws/describe-db-instances)))
  (route/not-found "Not Found"))

(def app
  (-> (wrap-defaults app-routes site-defaults)
      allow-cross-origin))