(ns om-pm.util)

(defn set-transfer-data! [e key value]
  (.setData (-> e .-nativeEvent .-dataTransfer)
            key value))

(defn get-transfer-data! [e key]
  (-> (-> e .-nativeEvent .-dataTransfer)
      (.getData key)))