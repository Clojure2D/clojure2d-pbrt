(defproject pbrt "0.0.1"
  :description "Clojure version of PBRT"
  :plugins [[refactor-nrepl "2.4.0-SNAPSHOT"]
            [cider/cider-nrepl "0.16.0-SNAPSHOT"]
            [lein-expectations "0.0.8"]
            [lein-autoexpect "1.9.0"]]
  :url "https://github.com/Clojure2D/clojure2d-pbrt"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.9.0-beta1"]
                 [expectations "2.2.0-rc1"]
                 [criterium "0.4.4"]
                 [clojure2d "0.0.5-SNAPSHOT"]]
  :java-source-paths ["src"]
  :repl-options {:timeout 120000}
  :target-path "target/%s"
  :jvm-opts ["-Xmx4096M"
                                        ;   "-Dcom.sun.management.jmxremote"
                                        ;   "-Dcom.sun.management.jmxremote.ssl=false"
                                        ;   "-Dcom.sun.management.jmxremote.authenticate=false"
                                        ;   "-Dcom.sun.management.jmxremote.port=43210"
             ]
  :profiles {:uberjar {:aot :all}})
