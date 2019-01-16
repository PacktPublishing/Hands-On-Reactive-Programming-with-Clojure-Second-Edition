(ns stock-market-monitor.04buffer
  (:require [rx.lang.clojure.core :as rx])
  (:import (java.util.concurrent TimeUnit)
           (rx Observable)))

(def values (range 10))

(doseq [buffer (partition 5 1 values)]
  (prn buffer))

(def  repl-out *out*)
(defn prn-to-repl [& args]
  (binding [*out* repl-out]
    (apply prn args)))

(-> (rx/seq->o (vec (range 10)))
    (.buffer 5 1)
    (rx/subscribe
     (fn [price]
       (prn (str "Value: " price)))))

(defn -main [& args]
  (println "main method run"))
