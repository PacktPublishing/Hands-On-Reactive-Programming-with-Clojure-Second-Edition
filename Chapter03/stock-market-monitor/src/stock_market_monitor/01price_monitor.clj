(ns stock-market-monitor.01price-monitor
  (:require [seesaw.core :refer :all])
  (:import (java.util.concurrent ScheduledThreadPoolExecutor
                                 TimeUnit)))

(native!)

(def main-frame (frame :title "Stock price monitor"
                       :width 200 :height 100
                       :on-close :exit))

(def price-label       (label "Price: -"))

(config! main-frame :content price-label)

(def pool (atom nil))

(defn init-scheduler [num-threads]
  (reset! pool  (ScheduledThreadPoolExecutor. num-threads)))

(defn run-every [pool millis f]
  (.scheduleWithFixedDelay pool
                           f
                           0 millis TimeUnit/MILLISECONDS))

(defn shutdown [pool]
  (println "Shutting down scheduler...")
  (.shutdown pool))

(defn share-price [company-code]
  (Thread/sleep 200)
  (rand-int 1000))

(defn -main [& args]
  (show! main-frame)
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. #(shutdown @pool)))
  (init-scheduler 1)
  (run-every @pool 500
             #(->> (str "Price: " (share-price "XYZ"))
                   (text! price-label)
                   invoke-now)))
