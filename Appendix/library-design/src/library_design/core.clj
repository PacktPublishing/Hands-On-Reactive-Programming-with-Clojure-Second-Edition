(ns library-design.core
  (:require [imminent.core :as i]
            [rx.lang.clojure.core :as rx]
            [clojure.core.async :as async]))


(def  repl-out *out*)
(defn prn-to-repl [& args]
  (binding [*out* repl-out]
    (apply prn args)))

(-> (i/const-future 31)
    (i/map #(* % 2))
    (i/on-success #(prn-to-repl (str "Value: " %))))


(as-> (rx/return 31) obs
      (rx/map #(* % 2) obs)
      (rx/subscribe obs #(prn-to-repl (str "Value: " %))))

(def c        (async/chan))
(def mapped-c (async/map< #(* % 2) c))

(async/go (async/>! c 31))
(async/go (prn-to-repl (str "Value: " (async/<! mapped-c))))
