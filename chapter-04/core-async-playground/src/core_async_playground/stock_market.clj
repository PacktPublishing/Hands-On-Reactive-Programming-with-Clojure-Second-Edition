(ns core-async-playground.stock-market
  (:require [clojure.core.async
             :refer [go chan <! >! timeout go-loop map>] :as async])
  (:require [clojure.core.async.lab :refer [broadcast]])
  (:use [seesaw.core]))

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

(defn roll-buffer [buffer val buffer-size]
  (let [buffer (conj buffer val)]
    (if (> (count buffer) buffer-size)
      (pop buffer)
      buffer)))

(defn make-sliding-buffer [buffer-size]
  (let [buffer (atom clojure.lang.PersistentQueue/EMPTY)]
    (fn [n]
      (swap! buffer roll-buffer n buffer-size))))

(def sliding-buffer (make-sliding-buffer 5))

(defn broadcast-at-interval [msecs task & ports]
  (go-loop [out (apply broadcast ports)]
    (<! (timeout msecs))
    (>! out (task))
    (recur out)))

(defn -main [& args]
  (show! main-frame)
  (let [prices-ch         (chan)
        sliding-buffer-ch (map> sliding-buffer (chan))]
    (broadcast-at-interval 500 #(share-price "XYZ") prices-ch sliding-buffer-ch)
    (go-loop []
      (when-let [price (<! prices-ch)]
        (text! price-label (str "Price: " price))
        (recur)))
    (go-loop []
      (when-let [buffer (<! sliding-buffer-ch)]
        (text! running-avg-label (str "Running average: " (avg buffer)))
        (recur)))))
