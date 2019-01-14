(ns reagi-game.entities
  (:require [monet.canvas :as canvas]
            [monet.geometry :as geom]))

(defn shape-x [shape]
  (-> shape :pos deref :x))

(defn shape-y [shape]
  (-> shape :pos deref :y))

(defn shape-angle [shape]
  @(:angle shape))

(defn shape-data [x y angle]
  {:pos   (atom {:x x :y y})
   :angle (atom angle)})

(defn ship-entity [ship]
  (canvas/entity {:x (shape-x ship) :y (shape-y ship) :angle (shape-angle ship)}
                 (fn [value]
                   (-> value
                       (assoc :x     (shape-x ship))
                       (assoc :y     (shape-y ship))
                       (assoc :angle (shape-angle ship))))
                 (fn [ctx {:keys [x y angle]}]
                   (-> ctx
                       canvas/save
                       (canvas/translate x y)
                       (canvas/rotate angle)
                       (canvas/begin-path)
                       (canvas/move-to 50 0)
                       (canvas/line-to 0 -15)
                       (canvas/line-to 0 15)
                       (canvas/fill)
                       canvas/restore))))

(declare move-forward!)

(defn make-bullet-entity [monet-canvas key shape]
  (canvas/entity {:x (shape-x shape) :y (shape-y shape) :angle (shape-angle shape)}
                 (fn [value]
                   (when-not
                           (geom/contained?
                             {:x 0 :y 0
                              :w (.-width (:canvas monet-canvas))
                              :h (.-height (:canvas monet-canvas))}
                             {:x (shape-x shape) :y (shape-y shape) :r 5})
                     (canvas/remove-entity monet-canvas key))
                   (move-forward! shape)
                   (assoc value :x (shape-x shape) :y (shape-y shape) :angle (shape-angle shape)))
                 (fn [ctx {:keys [x y angle]}]
                   (-> ctx
                       canvas/save
                       (canvas/translate x y)
                       (canvas/rotate angle)
                       (canvas/fill-style "red")
                       (canvas/circle {:x 10 :y 0 :r 5})
                       canvas/restore))))

(def speed 200)

;;x' = x cos f - y sin f

(defn calculate-x [angle]
  (* speed (/ (* (Math/cos angle)
                 Math/PI)
              180)))

(defn calculate-y [angle]
  (* speed (/ (* (Math/sin angle)
                 Math/PI)
              180)))

(defn move! [shape f]
  (let [pos (:pos shape)]
    (swap! pos (fn [xy]
                 (-> xy
                     (update-in [:x]
                                #(f % (calculate-x
                                        (shape-angle shape))))
                     (update-in [:y]
                                #(f % (calculate-y
                                        (shape-angle shape)))))))))


(defn move-forward! [shape]
  (move! shape +))

(defn move-backward! [shape]
  (move! shape -))

(defn rotate! [shape f]
  (swap! (:angle shape) #(f % (/ (/ Math/PI 3) 20))))

(defn rotate-right! [shape]
  (rotate! shape +))

(defn rotate-left! [shape]
  (rotate! shape -))

(defn fire! [monet-canvas ship]
  (let [entity-key (keyword (gensym "bullet"))
        data (shape-data (shape-x ship)
                         (shape-y ship)
                         (shape-angle ship))
        bullet (make-bullet-entity monet-canvas
                                   entity-key
                                   data)]
    (canvas/add-entity monet-canvas entity-key bullet)))
