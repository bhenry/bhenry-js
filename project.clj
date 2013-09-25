(defproject javascripts "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [jayq "2.4.0"]
                 [prismatic/dommy "0.1.1"]]
  :plugins [[lein-cljsbuild "0.3.0"]]
  :source-path "src/clj"
  :cljsbuild {:builds [{:source-paths ["src/cljs"]
                        :compiler {:pretty-print true
                                   :output-to "../assets/js/main.js"
                                   :optimization :whitespace}}]})
