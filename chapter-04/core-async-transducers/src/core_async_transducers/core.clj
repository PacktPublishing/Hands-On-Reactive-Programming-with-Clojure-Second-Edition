(ns core-async-transducers.core)

(require '[clojure.core.async :refer [go chan map< filter< into >! <! go-loop close! pipe]])

(->> (range 10)
         (map inc)           ;; creates a new sequence
         (filter even?)      ;; creates a new sequence
         (prn "result is "))

(def xform
      (comp (map inc)
            (filter even?)))  ;; no intermediate sequence created

(->> (range 10)
     (sequence xform)
     (prn "result is "))

(def result (chan 10))

(def transformed
  (->> result
       (map< inc)      ;; creates a new channel 
       (filter< even?) ;; creates a new channel 
       (into [])))

(go
  (prn "result is " (<! transformed)))

(go
  (doseq [n (range 10)]
    (>! result n))
  (close! result))

(def result (chan 10))

(def xform
     (comp (map inc)
           (filter even?)))  ;; no intermediate channels created

(def transformed (->> (pipe result (chan 10 xform))
                      (into [])))

(go
  (prn "result is " (<! transformed)))

(go
  (doseq [n (range 10)]
    (>! result n))
  (close! result))
