(ns restful.util
  (:require [clojure.string :as str]))

(def non-blank? (complement str/blank?))

(defn max-length? [length text]
  (<= (count text) length))

(defn non-blank-with-max-length? [length text]
  (and (non-blank? text) (max-length? length text)))

(defn min-length? [length text]
  (>= (count text) length))

(defn length-in-range? [min-length max-length text]
  (and (min-length? min-length text) (max-length? max-length text)))