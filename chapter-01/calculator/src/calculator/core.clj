(ns calculator.core
  (:require [seesaw.core :refer :all]))

(native!)

(def main-frame (frame :title "Calculator" :on-close :exit))

(def field-x (text "1"))
(def field-y (text "2"))

(def result-label (label "Type numbers in the boxes to add them up!"))

(defn update-sum [e]
  (try
    (text! result-label
         (str "Sum is " (+ (Integer/parseInt (text field-x))
                           (Integer/parseInt (text field-y)))))
    (catch Exception e
      (println "Error parsing input."))))

(listen field-x :key-released update-sum)
(listen field-y :key-released update-sum)

(config! main-frame :content
         (border-panel
          :north (horizontal-panel :items [field-x field-y])
          :center result-label
          :border 5))

(defn -main [& args]
  (-> main-frame pack! show!))