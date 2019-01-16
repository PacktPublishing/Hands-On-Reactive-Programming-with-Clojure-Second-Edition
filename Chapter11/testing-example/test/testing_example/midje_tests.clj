(ns testing-example.midje-tests
  (:require [midje.sweet :refer :all]
            [testing-example.core :refer :all]))

(fact (non-blank? "Some text") => true)

(facts "nil or blank strings are invalid values"
       (fact (non-blank? nil) => false)
       (fact (non-blank? "") => false))

(facts "alpha-numeric are possible values"
       (fact (non-blank? "Some text") => true)
       (fact (non-blank? "1234") => true))

(unfinished speclj-max-length?)

(def text-title "Some title")
(def text-max-length 20)

(comment (defn non-blank-with-max-length? [length text]))

(defn non-blank-with-max-length? [length text]
  (and (non-blank? text) (speclj-max-length? text length)))

(fact "Title should be non blank and 20 characters maximum"
      (non-blank-with-max-length? text-max-length text-title) => true
      (provided
        (speclj-max-length? text-title text-max-length) => true))

(defn max-length? [length text])

(defn max-length? [length text]
  (<= (count text) length))

(fact "Text max length should not exceed provided number"
      (max-length? text-max-length text-title) => true)

(fact "Passing text longer than max value"
      (max-length? (- (count text-title) 1) text-title) => false)

(fact "Passing a number throws an exception"
      (non-blank? 1234) => (throws ClassCastException))

(fact "Check if a collection contains some elements"
      [:bread :butter :lemon] => (contains [:bread :butter :lemon]))

(comment (fact "The order of elements matters"
               [:bread :butter :lemon] => (contains [:lemon :bread :butter])))

(fact "The order of elements matters in not taken into account"
      [:bread :butter :lemon] => (contains [:lemon :bread :butter] :in-any-order))

(comment (fact "Collection has more elements"
               [:bread :butter :cake :lemon] => (contains [:bread :butter :lemon])))

(fact "Collection has more elements"
      [:bread :butter :cake :lemon] => (contains [:bread :butter :lemon] :gaps-ok))

(fact "Collection has more elements and not in order"
      [:lemon :bread :butter :cake] => (contains [:bread :butter :lemon] :gaps-ok :in-any-order))

(comment (fact "Collection has too many elements"
               [:bread :butter :cake] => (just [:bread :butter])))

(fact "Collection has exact number of elements"
      [:bread :butter] => (just [:bread :butter]))