(ns clj-futures-playground.core
  (:require [clojure.pprint :refer [pprint]]))

(def movie
  {:name "Lord of The Rings: The Fellowship of The Ring"
   :cast ["Cate Blanchett"
          "Elijah Wood"
          "Liv Tyler"
          "Orlando Bloom"]})

(def actor-movies
  [{:name "Cate Blanchett"
    :movies ["Lord of The Rings: The Fellowship of The Ring"
             "Lord of The Rings: The Return of The King"
             "The Curious Case of Benjamin Button"]}

   {:name "Elijah Wood"
    :movies ["Eternal Sunshine of the Spotless Mind"
             "Green Street Hooligans"
             "The Hobbit: An Unexpected Journey"]}

   {:name "Liv Tyler"
    :movies ["Lord of The Rings: The Fellowship of The Ring"
             "Lord of The Rings: The Return of The King"
             "Armageddon"]}

   {:name "Orlando Bloom"
    :movies ["Lord of The Rings: The Fellowship of The Ring"
             "Lord of The Rings: The Return of The King"
             "Pirates of the Caribbean: The Curse of the Black Pearl"]}])

(def actor-spouse
  [{:name "Cate Blanchett"    :spouse "Andrew Upton"}
   {:name "Elijah Wood"       :spouse "Unknown"}
   {:name "Liv Tyler"         :spouse "Royston Langdon"}
   {:name "Orlando Bloom"     :spouse "Miranda Kerr"}])

(def top-5-movies
  ["Lord of The Rings: The Fellowship of The Ring"
   "The Matrix"
   "The Matrix Reloaded"
   "Pirates of the Caribbean: The Curse of the Black Pearl"
   "Terminator"])

(defn cast-by-movie [name]
  (future (do (Thread/sleep 5000)
              (:cast  movie))))

(defn movies-by-actor [name]
  (do (Thread/sleep 2000)
      (->> actor-movies
           (filter #(= name (:name %)))
           first)))

(defn spouse-of [name]
  (do (Thread/sleep 2000)
      (->> actor-spouse
           (filter #(= name (:name %)))
           first)))

(defn top-5 []
  (future (do (Thread/sleep 5000)
              top-5-movies)))

(defn aggregate-actor-data [spouses movies top-5]
  (map (fn [{:keys [name spouse]} {:keys [movies]}]
         {:name   name
          :spouse spouse
          :movies (map (fn [m]
                         (if (some #{m} top-5)
                           (str m " - (top 5)")
                           m))
                       movies)})
       spouses
       movies))

(defn -main [& args]
  (time (let [cast    (cast-by-movie "Lord of The Rings: The Fellowship of The Ring")
              movies  (pmap movies-by-actor @cast)
              spouses (pmap spouse-of @cast)
              top-5   (top-5)]
          (prn "Fetching data...")
          (pprint (aggregate-actor-data spouses movies @top-5))
          (shutdown-agents))))
