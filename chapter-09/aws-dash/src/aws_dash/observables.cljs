(ns aws-dash.observables
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [cognitect.transit :as t]))


(def r (t/reader :json))

(def  aws-endpoint "http://localhost:3001")
(defn aws-uri [path]
  (str aws-endpoint path))

(def rx-filter js/rxjs.operators.filter)
(def rx-flat-map js/rxjs.operators.flatMap)
(def rx-map js/rxjs.operators.map)
(def rx-merge js/rxjs.merge)
(def rx-reduce js/rxjs.operators.reduce)

(defn observable-seq [uri transform]
  (.create js/rxjs.Observable
           (fn [observer]
             (go (let [response      (<! (http/get uri {:with-credentials? false}))
                       data          (t/read r (:body response))
                       transformed   (transform data)]
                   (doseq [x transformed]
                     (.next observer x))
                   (.complete observer)))
             (fn [] (.log js/console "Disposed")))))

(defn describe-stacks []
  (observable-seq (aws-uri "/cloudFormation/describeStacks")
                  (fn [data]
                    (map (fn [stack] {:stack-id   (stack "StackId")
                                     :stack-name (stack "StackName")})
                         (data "Stacks")))))


(defn describe-stack-resources [stack-name]
  (observable-seq (aws-uri "/cloudFormation/describeStackResources")
                  (fn [data]
                    (map (fn [resource]
                           {:resource-id (resource "PhysicalResourceId")
                            :resource-type (resource "ResourceType")} )
                         (data "StackResources")))))

(defn describe-instances [instance-ids]
  (observable-seq (aws-uri "/ec2/describeInstances")
                  (fn [data]
                    (let [instances (mapcat (fn [reservation] (reservation "Instances")) (data "Reservations"))]
                      (map (fn [instance]
                             {:instance-id  (instance "InstanceId")
                              :type        "EC2"
                              :status      (get-in instance ["State" "Name"])})
                           instances)))))

(defn describe-db-instances [instance-ids]
  (observable-seq (aws-uri "/rds/describeDBInstances")
                  (fn [data]
                    (map (fn [instance]
                           {:instance-id (instance "DBInstanceIdentifier")
                            :type        "RDS"
                            :status      (instance "DBInstanceStatus")})
                         (data "DBInstances")))))

(defn stack-resources []
  (-> (describe-stacks)
      (.pipe (rx-map #(:stack-name %)))
      (.pipe (rx-flat-map describe-stack-resources))))

(defn ec2-instance-status [resources]
  (-> resources
      (.pipe (rx-filter #(= (:resource-type %) "AWS::EC2::Instance")))
      (.pipe (rx-map #(:resource-id %)))
      (.pipe (rx-reduce conj []))
      (.pipe (rx-flat-map describe-instances))))

(defn rds-instance-status [resources]
  (-> resources
      (.pipe (rx-filter #(= (:resource-type %) "AWS::RDS::DBInstance")))
      (.pipe (rx-map #(:resource-id %)))
      (.pipe (rx-reduce conj []))
      (.pipe (rx-flat-map describe-db-instances))))