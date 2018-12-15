(ns testing-example.core
  (:require [clojure.string :as str]))

(def non-blank? (complement str/blank?))