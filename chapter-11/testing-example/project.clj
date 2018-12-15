(defproject testing-example "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [expectations "2.2.0-rc3"]
                 [midje "1.9.4"]
                 [speclj "3.3.2"]]
  :plugins [[lein-expectations "0.0.8"]
            [speclj "3.3.2"]])
