(defproject cage-match "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.40"]
                 [org.clojure/core.async "0.2.374"]
                 [figwheel-sidecar "0.5.0-2" :scope "test"]
                 [com.cemerick/piggieback "0.2.1"]
                 [org.omcljs/om "1.0.0-alpha26"]
                 [cljs-audiocapture "0.1.4"]
                 [cljsjs/filesaverjs "1.1.20151003-0"]]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]
                :figwheel true
                :compiler {:main cage-match.core
                           :asset-path "js"
                           :output-to "resources/public/js/main.js"
                           :output-dir "resources/public/js"
                           :verbose true}}]}

  :plugins [[lein-cljsbuild "1.0.5"]
            [lein-environ "1.0.0"]]

  :profiles {:dev {:plugins [[lein-figwheel "0.3.9"]]
                   :figwheel {:http-server-root "public"
                              :port 3449
                              :nrepl-port 7888
                              :css-dirs ["resources/public/css"]}
                   :env {:environment "dev"}
                   :source-paths ["src"]
                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]
                                              :figwheel true}}}}
             :repl {:plugins [[cider/cider-nrepl "0.10.1"]]}
             :test [:dev {:env {:environment "test"
                                :plugins [[test2junit "1.1.0"]]}}]}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]
                 :timeout 1200000})
