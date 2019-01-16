(ns imminent-playground.repl
  (:require [imminent.core :as i]))

(def  repl-out *out*)
(defn prn-to-repl [& args]
  (binding [*out* repl-out]
    (apply prn args)))

(def age (i/future 31))

;; #<Future@2ea0ca7d: #<Success@3e4dec75: 31>>

(def failed-computation   (i/future (throw (Exception. "Error"))))
;; #<Future@63cd0d58: #<Failure@2b273f98: #<Exception java.lang.Exception: Error>>>

(def failed-computation-1 (i/failed-future :invalid-data))
;; #<Future@a03588f: #<Failure@61ab196b: :invalid-data>>

@age           ;; #<Success@3e4dec75: 31>
(deref @age)   ;; 31
(i/dderef age) ;; 31


@(i/future (do (Thread/sleep 500)
               "hello"))
;; :imminent.future/unresolved


(def double-age (i/map age #(* % 2)))
;; #<Future@659684cb: #<Success@7ce85f87: 62>>

(i/on-success age #(prn-to-repl (str "Age is: " %)))
;; "Age is: 31"

(-> failed-computation
    (i/map #(* % 2)))
;; #<Future@7f74297a: #<Failure@2b273f98: #<Exception java.lang.Exception: Error>>>

(i/map (i/success "hello")
       #(str % " world"))
;; #<Success@714eea92: "hello world">

(i/map (i/failure "error")
       #(str % " world"))
;; #<Failure@6d685b65: "error">

(defn range-future [n]
  (i/const-future (range n)))

(def age-range (i/map age range-future))

;; #<Future@3d24069e: #<Success@82e8e6e: #<Future@2888dbf4: #<Success@312084f6: (0 1 2...)>>>>

(def age-range (i/flatmap age range-future))

;; #<Future@601c1dfc: #<Success@55f4bcaf: (0 1 2 ...)>>

(def name (i/future (do (Thread/sleep 500)
                        "Leo")))
(def genres (i/future (do (Thread/sleep 500)
                          ["Heavy Metal" "Black Metal" "Death Metal" "Rock 'n Roll"])))

(->  (i/sequence [name age genres])
     (i/on-success
      (fn [[name age genres]]
        (prn-to-repl (format "%s is %s years old and enjoys %s"
                             name
                             age
                             (clojure.string/join "," genres))))))

;; "Leo is 31 years old and enjoys Heavy Metal,Black Metal,Death Metal,Rock 'n Roll"

(defn calculate-double [n]
  (i/const-future (* n 2)))

(-> (i/map-future calculate-double [1 2 3 4])
    i/await
    i/dderef)

;; [2 4 6 8]
