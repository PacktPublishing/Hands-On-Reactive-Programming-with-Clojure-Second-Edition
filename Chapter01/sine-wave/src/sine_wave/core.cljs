(ns sine-wave.core)

(defn canvas []
  (.getElementById js/document "myCanvas"))

(defn ctx []
  (.getContext (canvas) "2d"))

(def rx-interval js/rxjs.interval)
(def rx-take js/rxjs.operators.take)
(def rx-map js/rxjs.operators.map)
(def app-time (rx-interval 10))

(.log js/console "hello clojurescript")

(-> app-time
    (.pipe (rx-take 5))
    (.subscribe (fn [n]
                  (.log js/console n))))

(defn deg-to-rad [n]
  (* (/ Math/PI 180) n))

(defn sine-coord [x]
  (let [sin (Math/sin (deg-to-rad x))
        y (- 100 (* sin 90))]
    {:x x
     :y y
     :sin sin}))

(.log js/console (str (sine-coord 50)))

(def sine-wave
  (.pipe app-time (rx-map sine-coord)))

(-> app-time
    (.pipe (rx-take 5))
    (.subscribe (fn [num]
                  (.log js/console (sine-coord num) ))))

(defn fill-rect [x y colour]
  (set! (.-fillStyle (ctx)) colour)
  (.fillRect (ctx) x y 2 2))

;;; Draw an orange line
(-> app-time
    (.pipe (rx-take 700))
    (.subscribe (fn [{:keys [x y]}]
                  (fill-rect x y "orange"))))

(def colour (.pipe sine-wave
                   (rx-map
                     (fn [{:keys [sin]}]
                       (if (< sin 0)
                         "red"
                         "blue")))))

;;; Draw an alternating line
(-> (js/rxjs.zip sine-wave colour)
    (.pipe (rx-take 700))
    (.subscribe (fn [[{:keys [x y]} colour]]
                  (fill-rect x y colour))))

(def red  (.pipe app-time (rx-map (fn [_] "red"))))
(def blue (.pipe app-time (rx-map (fn [_] "blue"))))

(def rx-concat     js/rxjs.concat)
(def rx-defer      js/rxjs.defer)
(def rx-from-event js/rxjs.fromEvent)
(def rx-takeUntil  js/rxjs.operators.takeUntil)


(def mouse-click (rx-from-event canvas "click"))

(def cycle-colour
  (rx-concat (.pipe red (rx-takeUntil mouse-click))
          (rx-defer #(rx-concat (.pipe blue (rx-takeUntil mouse-click))
                          cycle-colour))))

;;; Draw a line that reacts on click
(-> (js/rxjs.zip sine-wave cycle-colour)
    (.pipe (rx-take 700))
    (.subscribe (fn [[{:keys [x y]} colour]]
                  (fill-rect x y colour))))
