(ns rx-playground.core)


(require '[rx.lang.clojure.core :as rx])
(import '(rx Observable))

;;
;; Creating Observables
;;

(def obs (rx/return 10))

(rx/subscribe obs
              (fn [value]
                (prn (str "Got value: " value))))


(-> (rx/seq->o [1 2 3 4 5 6 7 8 9 10])
    (rx/subscribe prn))

(-> (rx/range 1 10)
    (rx/subscribe prn))


(import '(java.util.concurrent TimeUnit))

(def  repl-out *out*)
(defn prn-to-repl [& args]
  (binding [*out* repl-out]
    (apply prn args)))


(def subscription (rx/subscribe (Observable/interval 100 TimeUnit/MILLISECONDS)
                                prn-to-repl))

(Thread/sleep 1000)

(rx/unsubscribe subscription)

(defn just-obs [v]
  (rx/observable*
   (fn [observer]
     (rx/on-next observer v)
     (rx/on-completed observer))))

(rx/subscribe (just-obs 20) prn)


;;
;; Manipulating observables
;;

(rx/subscribe (->> (Observable/interval 1 TimeUnit/MICROSECONDS)
                   (rx/filter even?)
                   (rx/take 5)
                   (rx/reduce +))
                   prn-to-repl)


(defn musicians []
  (rx/seq->o ["James Hetfield" "Dave Mustaine" "Kerry King"]))

(defn bands     []
  (rx/seq->o ["Metallica" "Megadeth" "Slayer"]))

(defn uppercased-obs []
  (rx/map (fn [s] (.toUpperCase s)) (bands)))

(-> (rx/map vector
            (musicians)
            (uppercased-obs))
    (rx/subscribe (fn [[musician band]]
                    (prn-to-repl (str musician " - from: " band)))))


;;
;; Mapcatting / Flatmapping
;;

(defn factorial [n]
  (reduce * (range 1 (inc n))))

(defn all-positive-integers []
  (Observable/interval 1 TimeUnit/MICROSECONDS))

(defn fact-obs [n]
  (rx/observable*
   (fn [observer]
     (rx/on-next observer (factorial n))
     (rx/on-completed observer))))

(rx/subscribe (fact-obs 5) prn-to-repl)


(rx/subscribe (->> (all-positive-integers)
                   (rx/filter  even?)
                   (rx/flatmap fact-obs)
                   (rx/take 5))
              prn-to-repl)

(defn repeat-obs [n]
  (rx/seq->o (repeat 2 n)))

(-> (repeat-obs 5)
    (rx/subscribe prn-to-repl))


(rx/subscribe (->> (all-positive-integers)
                   (rx/flatmap repeat-obs)
                   (rx/take 6))
              prn-to-repl)


(defn exceptional-obs []
  (rx/observable*
   (fn [observer]
     (rx/on-next observer (throw (Exception. "Oops. Something went wrong")))
     (rx/on-completed observer))))

(rx/subscribe (->> (exceptional-obs)
                   (rx/map inc))
              (fn [v] (prn-to-repl "result is " v)))

(rx/subscribe (->> (exceptional-obs)
                   (rx/map inc))
              (fn [v] (prn-to-repl "result is " v))
              (fn [e] (prn-to-repl "error is " e)))

(rx/subscribe (->> (exceptional-obs)
                   (rx/catch Exception e
                       (rx/return 10))
                   (rx/map inc))
              (fn [v] (prn-to-repl "result is " v)))

(rx/subscribe (->> (exceptional-obs)
                   (rx/catch Exception e
                     (rx/seq->o (range 5)))
                   (rx/map inc))
              (fn [v] (prn-to-repl "result is " v)))

(defn retry-obs []
  (let [errored (atom false)]
    (rx/observable*
     (fn [observer]
       (if @errored
         (rx/on-next observer 20)
         (do (reset! errored true)
             (throw (Exception. "Oops. Something went wrong"))))))))

(rx/subscribe (retry-obs)
              (fn [v] (prn-to-repl "result is " v)))


;;; Backpressure

(defn fast-producing-obs []
  (rx/map inc (Observable/interval 1 TimeUnit/MILLISECONDS)))

(defn slow-producing-obs []
  (rx/map inc (Observable/interval 500 TimeUnit/MILLISECONDS)))

;;; Sample

(rx/subscribe (->> (rx/map vector
                           (fast-producing-obs)
                           (slow-producing-obs))
                   (rx/map (fn [[x y]]
                             (+ x y)))
                   (rx/take 10))
              prn-to-repl
              (fn [e] (prn-to-repl "error is " e)))

(rx/subscribe (->> (rx/map vector
                           (.sample (fast-producing-obs) 200
                                    TimeUnit/MILLISECONDS)
                           (slow-producing-obs))
                   (rx/map (fn [[x y]]
                             (+ x y)))
                   (rx/take 10))
              prn-to-repl
              (fn [e] (prn-to-repl "error is " e)))

(rx/subscribe (->> (rx/map vector
                           (.onBackpressureBuffer (fast-producing-obs))
                           (slow-producing-obs))
                   (rx/map (fn [[x y]]
                             (+ x y)))
                   (rx/take 10))
              prn-to-repl
              (fn [e] (prn-to-repl "error is " e)))
