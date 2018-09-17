(ns respondent-app.core
  (:require [respondent.core :as r]
            [dommy.core :as dommy]))

(enable-console-print!)

(def mouse-pos-stream (r/event-stream))
(set! (.-onmousemove js/document)
      (fn [e]
        (r/deliver mouse-pos-stream [(.-pageX e) (.-pageY e)])))

(r/subscribe mouse-pos-stream
             (fn [[x y]]
               (dommy/set-text! (dommy/sel1 :#mouse-xy)
                                (str "(" x "," y ")"))))


(defn on-js-reload [])