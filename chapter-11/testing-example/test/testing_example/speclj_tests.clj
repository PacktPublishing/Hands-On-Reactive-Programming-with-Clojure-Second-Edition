(ns testing-example.speclj-tests
  (:require [speclj.core :refer :all]
            [testing-example.core :refer :all]))

(describe "Testing non-blank? function"
          (context "Passing invalid values"
                   (it "is passed an empty string"
                       (should-not (non-blank? "")))
                   (it "is passed a nil"
                       (should-not (non-blank? nil))))
          (context "Passing valid values"
                   (it "accepts letters"
                       (should (non-blank? "Some text")))
                   (it "accepts number strings"
                       (comment (should (non-blank? 1234)))
                       (should (non-blank? "1234")))))

(describe "Testing containment"
          (context "Using a string"
                   (it "looks for a substring"
                       (should-contain "Text" "Some Text"))
                   (it "looks for regular expression"
                       (should-contain #"Some.*" "Some text")))
          (context "Using collections"
                   (it "looks for a key in a map"
                       (should-contain :bird {:color :red :bird :parrot}))
                   (it "looks for a value in a collection"
                       (should-contain :orange [:blue :green :orange :pink]))))

(describe "Testing equivalency"
          (context "same order of items in collections"
                   (it "compares collections"
                       (should= [:bread :cake :juice] [:bread :cake :juice])))
          (context "different order of items in collections"
                   (it "compares collections"
                       (should==  [:bread :cake :juice] [:juice :cake :bread]))))

(describe "Testing return type"
          (it "return boolean"
              (should-be-a Boolean (non-blank? "text"))))

(describe "Testing exceptions"
          (it "throws ClassCastException"
              (should-throw ClassCastException (non-blank? 1234))))