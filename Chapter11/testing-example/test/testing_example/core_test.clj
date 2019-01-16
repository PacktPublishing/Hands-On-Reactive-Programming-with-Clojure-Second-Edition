(ns testing-example.core-test
  (:require [clojure.test :refer :all]
            [testing-example.core :refer :all]))

(deftest a-test
  (testing "Testing equality of a number."
    (is (= 1 1))))

(deftest non-blank-test
  (testing "Testing non-blank? function"
    (is (true? (non-blank? "Some text"))
        "Tested string is blank.")))

(deftest non-blank-test-with-is
  (testing "Testing non-blank? function"
    (is (true? (non-blank? "Some text")))
    (is (false? (non-blank? "")))
    (is (false? (non-blank? nil)))))

(deftest non-blank-test-with-are
  (testing "Testing non-blank? function"
    (are [predicate text] (predicate (non-blank? text))
                          true? "Some text"
                          false? ""
                          false? nil)))