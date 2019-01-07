(ns library-design.option
  (:require [uncomplicate.fluokitten.protocols :as fkp]
            [uncomplicate.fluokitten.core :as fkc]
            [uncomplicate.fluokitten.jvm :as fkj]
            [imminent.core :as i]))

;;
;; Functor
;;

(def pirates [{:name "Jack Sparrow"    :born 1700 :died 1740 :ship "Black Pearl"}
              {:name "Blackbeard"      :born 1680 :died 1750 :ship "Queen Anne's Revenge"}
              {:name "Hector Barbossa" :born 1680 :died 1740 :ship nil}])


(defn pirate-by-name [name]
  (->> pirates
       (filter #(= name (:name %)))
       first))

(defn age [{:keys [born died]}]
  (- died born))

(comment

  (-> (pirate-by-name "Jack Sparrow")
      age) ;; 40

  (-> (pirate-by-name "Davy Jones")
      age) ;; NullPointerException   clojure.lang.Numbers.ops (Numbers.java:961)

  )

(defrecord Some [v])

(defrecord None [])


(defn option [v]
  (if v
    (Some. v)
    (None.)))


(extend-protocol fkp/Functor
  Some
  (fmap [f g]
    (Some. (g (:v f))))
  None
  (fmap [_ _]
    (None.)))


(->> (option (pirate-by-name "Jack Sparrow"))
     (fkc/fmap age)) ;; #library_design.option.Some{:v 40}

(->> (option (pirate-by-name "Davy Jones"))
     (fkc/fmap age)) ;; #library_design.option.None{}


(->> (option (pirate-by-name "Jack Sparrow"))
     (fkc/fmap age)
     (fkc/fmap inc)
     (fkc/fmap #(* 2 %))) ;; #library_design.option.Some{:v 82}

(->> (option (pirate-by-name "Davy Jones"))
     (fkc/fmap age)
     (fkc/fmap inc)
     (fkc/fmap #(* 2 %))) ;; #library_design.option.None{}



(some-> (pirate-by-name "Davy Jones")
        age
        inc
        (* 2)) ;; nil


(->> (i/future (pirate-by-name "Jack Sparrow"))
     (fkc/fmap age)
     (fkc/fmap inc)
     (fkc/fmap #(* 2 %))) ;; #<Future@30518bfc: #<Success@39bd662c: 82>>


;; Functor laws

;; Identity
(= (fkc/fmap identity (option 1))
   (identity (option 1))) ;; true


;; Composition
(= (fkc/fmap (comp identity inc) (option 1))
   (fkc/fmap identity (fkc/fmap inc (option 1)))) ;; true


;;
;; Applicative
;;

(defn avg [& xs]
  (float (/ (apply + xs) (count xs))))

(comment



  (let [a (some-> (pirate-by-name "Jack Sparrow") age)
        b (some-> (pirate-by-name "Blackbeard") age)
        c (some-> (pirate-by-name "Hector Barbossa") age)]
    (avg a b c)) ;; 56.666668

  (let [a (some-> (pirate-by-name "Jack Sparrow") age)
        b (some-> (pirate-by-name "Davy Jones") age)
        c (some-> (pirate-by-name "Hector Barbossa") age)]
    (avg a b c)) ;; NullPointerException   clojure.lang.Numbers.ops (Numbers.java:961)

  (let [a (some-> (pirate-by-name "Jack Sparrow") age)
        b (some-> (pirate-by-name "Davy Jones") age)
        c (some-> (pirate-by-name "Hector Barbossa") age)]
    (when (and a b c)
      (avg a b c))) ;; nil

  )


(defprotocol Applicative 
  (pure [av v]) 
  (fapply [ag av]))

(extend-protocol fkp/Applicative
  Some
  (pure [_ v]
    (Some. v))

  (fapply [ag av]
    (if-let [v (:v av)]
      (Some. ((:v ag) v))
      (None.)))

  None
  (pure [_ v]
    (Some. v))

  (fapply [ag av]
    (None.)))


(fkc/fapply (option inc) (option 2))
;; #library_design.option.Some{:v 3}

(fkc/fapply (option nil) (option 2))
;; #library_design.option.None{}

(def age-option (comp (partial fkc/fmap age) option pirate-by-name))

(let [a (age-option "Jack Sparrow")
      b (age-option "Blackbeard")
      c (age-option "Hector Barbossa")]
  (fkc/<*> (option (fkj/curry avg 3))
           a b c))
;; #library_design.option.Some{:v 56.666668}

(def curried-1 (fkj/curry + 2)) 
(def curried-2 (fn [a] 
                 (fn [b] 
                   (+ a b)))) 
 
((curried-1 10) 20) ;; 30 
((curried-2 10) 20) ;; 30 





(defn alift
  "Lifts a n-ary function `f` into a applicative context"
  [f]
  (fn [& as]
    {:pre  [(seq as)]}
    (let [curried (fkj/curry f (count as))]
      (apply fkc/<*>
             (fkc/fmap curried (first as))
             (rest as)))))


(let [a (age-option "Jack Sparrow")
      b (age-option "Blackbeard")
      c (age-option "Hector Barbossa")]
  ((alift avg) a b c))
;; #library_design.option.Some{:v 56.666668}

((alift avg) (age-option "Jack Sparrow")
             (age-option "Blackbeard")
             (age-option "Hector Barbossa"))
;; #library_design.option.Some{:v 56.666668}

((alift avg) (age-option "Jack Sparrow")
             (age-option "Davy Jones")
             (age-option "Hector Barbossa"))
;; #library_design.option.None{}



((alift avg) (i/future (some-> (pirate-by-name "Jack Sparrow") age))
             (i/future (some-> (pirate-by-name "Blackbeard") age))
             (i/future (some-> (pirate-by-name "Hector Barbossa") age)))
;; #<Future@17b1be96: #<Success@16577601: 56.666668>>


;;
;; Monad
;;

(defn median [& ns]
  (let [ns (sort ns)
        cnt (count ns)
        mid (bit-shift-right cnt 1)]
    (if (odd? cnt)
      (nth ns mid)
      (/ (+ (nth ns mid) (nth ns (dec mid))) 2))))

(defn std-dev [& samples]
  (let [n (count samples)
	mean (/ (reduce + samples) n)
	intermediate (map #(Math/pow (- %1 mean) 2) samples)]
    (Math/sqrt
     (/ (reduce + intermediate) n))))


(comment

  (let  [a       (some-> (pirate-by-name "Jack Sparrow")    age)
         b       (some-> (pirate-by-name "Blackbeard")      age)
         c       (some-> (pirate-by-name "Hector Barbossa") age)
         avg     (avg a b c)
         median  (median a b c)
         std-dev (std-dev a b c)]
    {:avg avg
     :median median
     :std-dev std-dev})
  ;; {:avg 56.666668,
  ;;  :median 60,
  ;;  :std-dev 12.472191289246473}


  (let  [a       (some-> (pirate-by-name "Jack Sparrow")    age)
         b       (some-> (pirate-by-name "Davy Jones")      age)
         c       (some-> (pirate-by-name "Hector Barbossa") age)
         avg     (avg a b c)
         median  (median a b c)
         std-dev (std-dev a b c)]
    {:avg avg
     :median median
     :std-dev std-dev})
  ;; NullPointerException   clojure.lang.Numbers.ops (Numbers.java:961)

  (let  [a       (some-> (pirate-by-name "Jack Sparrow")    age)
         b       (some-> (pirate-by-name "Davy Jones")      age)
         c       (some-> (pirate-by-name "Hector Barbossa") age)
         avg     (when (and a b c) (avg a b c))
         median  (when (and a b c) (median a b c))
         std-dev (when (and a b c) (std-dev a b c))]
    (when (and a b c)
      {:avg avg
       :median median
       :std-dev std-dev}))
  ;; nil


  )

(defprotocol Monad 
  (bind [mv g]))

(extend-protocol fkp/Monad
  Some
  (bind [mv g]
    (g (:v mv)))

  None
  (bind [_ _]
    (None.)))



;; (fkc/bind (None.) identity)

(def opt-ctx (None.))

(fkc/bind (age-option "Jack Sparrow")
          (fn [a]
            (fkc/bind (age-option "Blackbeard")
                      (fn [b]
                        (fkc/bind (age-option "Hector Barbossa")
                                  (fn [c]
                                    (fkc/pure opt-ctx
                                              (+ a b c))))))))
;; #library_design.option.Some{:v 170.0}

(fkc/mdo [a (age-option "Jack Sparrow")
          b (age-option "Blackbeard")
          c (age-option "Hector Barbossa")]
         (fkc/pure opt-ctx  (+ a b c)))
;; #library_design.option.Some{:v 170.0}

(require '[clojure.walk :as w])

(w/macroexpand-all '(fkc/mdo [a (age-option "Jack Sparrow")
                              b (age-option "Blackbeard")
                              c (age-option "Hector Barbossa")]
                             (fkc/pure opt-ctx  (+ a b c))))

;; (uncomplicate.fluokitten.core/bind
;;  (age-option "Jack Sparrow")
;;  (fn*
;;   ([a]
;;    (uncomplicate.fluokitten.core/bind
;;     (age-option "Blackbeard")
;;     (fn*
;;      ([b]
;;       (uncomplicate.fluokitten.core/bind
;;        (age-option "Hector Barbossa")
;;        (fn* ([c] (fkc/pure opt-ctx (+ a b c)))))))))))


(def avg-opt     (comp option avg))
(def median-opt  (comp option median))
(def std-dev-opt (comp option std-dev))

(fkc/mdo [a       (age-option "Jack Sparrow")
          b       (age-option "Blackbeard")
          c       (age-option "Hector Barbossa")
          avg     (avg-opt a b c)
          median  (median-opt a b c)
          std-dev (std-dev-opt a b c)]
         (fkc/pure opt-ctx {:avg avg
                  :median median
                  :std-dev std-dev}))
;; #library_design.option.Some{:v {:avg 56.666668,
;;                                 :median 60,
;;                                 :std-dev 12.472191289246473}}

(fkc/mdo [a       (age-option "Jack Sparrow")
          b       (age-option "Davy Jones")
          c       (age-option "Hector Barbossa")
          avg     (avg-opt a b c)
          median  (median-opt a b c)
          std-dev (std-dev-opt a b c)]
         (fkc/pure opt-ctx {:avg avg
                  :median median
                  :std-dev std-dev}))
;; #library_design.option.None{}

(def avg-fut     (comp i/future-call avg))
(def median-fut  (comp i/future-call median))
(def std-dev-fut (comp i/future-call std-dev))

(fkc/mdo [a       (i/future (some-> (pirate-by-name "Jack Sparrow") age))
          b       (i/future (some-> (pirate-by-name "Blackbeard") age))
          c       (i/future (some-> (pirate-by-name "Hector Barbossa") age))
          avg     (avg-fut a b c)
          median  (median-fut a b c)
          std-dev (std-dev-fut a b c)]
         (i/const-future {:avg avg
                          :median median
                          :std-dev std-dev}))
;; #<Future@3fd0b0d0: #<Success@1e08486b: {:avg 56.666668,
;;                                         :median 60,
;;                                         :std-dev 12.472191289246473}>>
