(ns stock-market-monitor.02price-monitor-rolling-avg
  (:require [seesaw.core :refer :all])
  (:import (java.util.concurrent ScheduledThreadPoolExecutor
                                 TimeUnit)
           (clojure.lang PersistentQueue)))

(native!)

(def main-frame (frame :title "Stock price monitor"
                       :width 200 :height 100
                       :on-close :exit))

(def price-label       (label "Price: -"))
(def running-avg-label (label "Running average: -"))

(config! main-frame :content
         (border-panel
          :north  price-label
          :center running-avg-label
          :border 5))

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

(defn roll-buffer [buffer num buffer-size]
  (let [buffer (conj buffer num)]
    (if (> (count buffer) buffer-size)
      (pop buffer)
      buffer)))

(defn avg [numbers]
  (float (/ (reduce + numbers)
            (count numbers))))

(defn make-running-avg [buffer-size]
  (let [buffer (atom clojure.lang.PersistentQueue/EMPTY)]
    (fn [n]
      (swap! buffer roll-buffer n buffer-size)
      (avg @buffer))))

(def running-avg (make-running-avg 5))

(defn worker []
  (let [price (share-price "XYZ")]
    (->> (str "Price: " price) (text! price-label))
    (->> (str "Running average: " (running-avg price))
         (text! running-avg-label))))

(defn -main [& args]
  (show! main-frame)
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. #(shutdown @pool)))
  (init-scheduler 1)
  (run-every @pool 500
             #(invoke-now (worker))))
