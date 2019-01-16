(ns core-async-playground.core
  (:require [clojure.core.async :refer [go chan <! >! timeout <!!]]))

(defn prn-with-thread-id [s]
  (prn (str s " - Thread id: " (.getId (Thread/currentThread)))))

(defn producer [c]
  (go (prn-with-thread-id "Taking a nap ")
      (<! (timeout 5000))
      (prn-with-thread-id "Now putting a name in que queue...")
      (>! c "Leo")))

(defn consumer [c]
  (go (prn-with-thread-id "Attempting to take value from queue now...")
      (prn-with-thread-id (str "Got it. Hello " (<! c) "!"))))

(def c (chan))

(consumer c)
(producer c)

;; Other example in the stock market section

(def c (chan))

(go (>! c 1)
    (>! c 2))

(go (prn-with-thread-id (<! c)))
(go (prn-with-thread-id (<! c)))

(defn -main [& args]
  (println "Main method run"))


;; sample output

"Attempting to take value from queue now... - Thread id: 43"
"Taking a nap  - Thread id: 44"
"Now putting a name in que queue... - Thread id: 48"
"Got it. Hello Leo! - Thread id: 48"
