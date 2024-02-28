(ns build
  (:require
   [clojure.tools.build.api :as b]
   [deps-deploy.deps-deploy :as dd]))

(def lib 'com.adgoji/mollie)
(def version "0.4.0")
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def jar-file (format "target/%s-%s.jar" (name lib) version))
(def scm-url "git@github.com:adgoji/mollie.git")

(defn clean [_]
  (println "Clean target dir")
  (b/delete {:path "target"}))

(defn jar [_]
  (println "Write pom")
  (b/write-pom {:class-dir class-dir
                :lib       lib
                :version   version
                :basis     basis
                :src-dirs  ["src" "spec"]
                :scm       {:tag                 version
                            :connection          (str "scm:git:git://github.com/adgoji/mollie")
                            :developerConnection (str "scm:git:ssh://git@github.com:adgoji/mollie.git")
                            :url                 "https://github.com/adgoji/mollie"}})
  (println "Copy sources and resources")
  (b/copy-dir {:src-dirs   ["src" "spec"]
               :target-dir class-dir})
  (println "Build jar")
  (b/jar {:class-dir class-dir
          :jar-file  jar-file})
  (println "Created" jar-file))

(defn release [_]
  (println "Release version" version)
  (b/git-process {:git-args ["tag" "--sign" version "-a" "-m" (str "Release " version)]})
  (println "Success!"))

(defn deploy [_]
  (println "Deploy to Clojars")
  (dd/deploy {:installer      :remote
              :artifact       (b/resolve-path jar-file)
              :pom-file       (b/pom-path {:lib       lib
                                           :class-dir class-dir})
              :sign-releases? false})
  (println "Deployed successfully"))
