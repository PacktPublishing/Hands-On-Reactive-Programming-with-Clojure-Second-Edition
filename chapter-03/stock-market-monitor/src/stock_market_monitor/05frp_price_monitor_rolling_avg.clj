(ns stock-market-monitor.05frp-price-monitor-rolling-avg
  (:require [rx.lang.clojure.core :as rx]
            [seesaw.core :refer :all])
  (:import (java.util.concurrent TimeUnit)
           (rx Observable)))

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

(defn share-price [company-code]
  (Thread/sleep 200)
  (rand-int 1000))

(defn avg [numbers]
  (float (/ (reduce + numbers)
            (count numbers))))

(defn make-price-obs [_]
  (rx/return (share-price "XYZ")))

(defn -main [& args]
  (show! main-frame)
  (let [price-obs (-> (rx/flatmap make-price-obs
                                  (Observable/interval 500 TimeUnit/MILLISECONDS))
                      (.publish))
        sliding-buffer-obs (.buffer price-obs 5 1)]
    (rx/subscribe price-obs
                  (fn [price]
                    (text! price-label (str "Price: " price))))
    (rx/subscribe sliding-buffer-obs
                  (fn [buffer]
                    (text! running-avg-label (str "Running average: " (avg buffer)))))
    (.connect price-obs)))
