(ns testing-example.expectations-tests
  (:require [expectations :refer :all]
            [testing-example.core :refer :all]))

(expect true (non-blank? "Some text"))

(expect ClassCastException (non-blank? 1234))

(expect Boolean (non-blank? ""))

(expect #"Dev" "DevOps")

(expect 2 (in [1 2 3 4 5 6]))

(expect {:surname "Doe"} (in {:name "John" :surname "Doe"}))

(expect 3 (in #{1 2 3 4}))