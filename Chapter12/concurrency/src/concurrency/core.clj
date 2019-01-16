(ns concurrency.core)

;;; Atoms

(def basket-counter (atom 0))

(deref basket-counter)

@basket-counter

(reset! basket-counter 3)

@basket-counter

(swap! basket-counter + 1)

@basket-counter

(swap! basket-counter (fn [existing-value] (+ existing-value 3)))

@basket-counter

(compare-and-set! basket-counter 7 8)

@basket-counter

;;; Agents

(def play-time (agent 0))

(deref play-time)

@play-time

(send play-time + 2)

@play-time

(send-off play-time - 1)

@play-time

;;; Refs

(def basket-items (ref []))

(deref basket-items)

@basket-items

(comment
  (ref-set basket-items ["vegetables"]))

(dosync
  (ref-set basket-items ["vegetables"]))

@basket-items

(dosync
  (alter basket-items (fn [current-value]
                        (conj current-value "fruits"))))

@basket-items

(def players-count (ref 0))


@players-count

(dosync
  (commute players-count
           (fn [current-value] (+ current-value 1))))

@players-count

(dosync
  (commute players-count
           (fn [current-value] (+ current-value 2))))


@players-count

;;; Vars

(def player-max-level 100)

player-max-level

(comment
  (binding [player-max-level 101]
    (println "player max level is: " player-max-level)))

(def ^:dynamic player-max-health 50)

player-max-health

(binding [player-max-health 55]
  (println "player max health is: " player-max-health))

(def ^:private items-count 14)

items-count

(def admin-name "John")

(defn great-player [player]
  (println admin-name " welcomes " player))

(great-player "Alice")

(great-player "Zack")

(def admin-name "Terry")

(great-player "Zack")

(def ^:const support-name "Stu")

(defn support-player [player]
  (println "Hi" player "it is" support-name "from support"))

(support-player "Zack")

(def support-name "Debbie")

(support-player "Zack")

;;; Futures

(defn get-user-accounts []
  (Thread/sleep 5000)
  [{:user "John"}{:user "Debbie"}])

(defn get-recent-purchases []
  (Thread/sleep 5000)
  ["vegetables" "fruits"])

(defn long-process []
  (let [users (get-user-accounts)
        purchases (get-recent-purchases)]
    {:purchases purchases
     :users users}))

(time (long-process))

(defn fast-process []
  (let [users (future (get-user-accounts))
        purchases (future (get-recent-purchases))]
    {:purchases @purchases
     :users @users}))

(time (fast-process))

;;; Promises

(let [prom (promise)]
  (future
    ;; do some work in the future
    (deliver prom :finished))
  @prom)


