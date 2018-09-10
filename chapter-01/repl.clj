(def numbers (atom []))

(defn adder [key ref old-state new-state]
  (print "Current sum is " (reduce + new-state)))

(add-watch numbers :adder adder)

(swap! numbers conj 1)
;; Current sum is  1

(swap! numbers conj 2)
;; Current sum is  3

(swap! numbers conj 7)
;; Current sum is  10

(->> [1 2 3 4 5 6]
     (map inc)
     (filter even?)
     (reduce +))
;; 12