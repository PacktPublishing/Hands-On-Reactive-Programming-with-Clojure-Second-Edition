(ns reagi-game.core
  (:require [monet.canvas :as canvas]
            [reagi.core :as r]
            [reagi-game.entities :as entities
             :refer [move-forward! move-backward! rotate-left! rotate-right! fire!]]))

(enable-console-print!)

(def canvas-dom (.getElementById js/document "canvas"))

(def monet-canvas (canvas/init canvas-dom "2d"))

(def ship (entities/shape-data (/ (.-width (:canvas monet-canvas)) 2)
                               (/ (.-height (:canvas monet-canvas)) 2)
                               0))

(def ship-entity (entities/ship-entity ship))

(canvas/add-entity monet-canvas :ship-entity ship-entity)
(canvas/draw-loop monet-canvas)

(def UP    38)
(def RIGHT 39)
(def DOWN  40)
(def LEFT  37)
(def FIRE  32) ;; space
(def PAUSE 80) ;; lower-case P

(defn keydown-stream []
  (let [out (r/events)]
    (set! (.-onkeydown js/document) #(r/deliver out [::down (.-keyCode %)]))
    out))

(defn keyup-stream []
  (let [out (r/events)]
    (set! (.-onkeyup   js/document) #(r/deliver out [::up (.-keyCode %)]))
    out))

(def active-keys-stream
  (->> (r/merge (keydown-stream) (keyup-stream))
       (r/reduce (fn [acc [event-type key-code]]
                   (condp = event-type
                     ::down (conj acc key-code)
                     ::up   (disj acc key-code)
                     acc))
                 #{})))

(defn filter-map [pred f & args]
  (->> active-keys-stream
       (r/filter (partial some pred))
       (r/map (fn [_] (apply f args)))))

(filter-map #{FIRE}  fire! monet-canvas ship)
(filter-map #{UP}    move-forward!  ship)
(filter-map #{DOWN}  move-backward! ship)
(filter-map #{RIGHT} rotate-right!  ship)
(filter-map #{LEFT}  rotate-left!   ship)

(defn pause! [_]
  (if @(:updating? monet-canvas)
    (canvas/stop-updating monet-canvas)
    (canvas/start-updating monet-canvas)))

(defn start-game []
  (->> active-keys-stream
       (r/filter (partial some #{PAUSE}))
       (r/throttle 100)
       (r/map pause!)))

(defn on-js-reload []
  (start-game))