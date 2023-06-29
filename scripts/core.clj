(ns core
  (:require
   [babashka.process :refer [shell]]
   [clojure.string :as str]))

(defn cljfmt
  "Run cljfmt binary with provided `args`.

  Throws an error if cljfmt binary is not available in the PATH."
  [& args]
  (assert (->> "which cljfmt" (shell {:out nil}) :exit zero?))
  (apply shell "cljfmt" args))

(defn clj-kondo
  "Run clj-kondo binary with provided `args`.

  Throws an error if clj-kondo binary is not available in the PATH."
  [& args]
  (assert (->> "which clj-kondo" (shell {:out nil}) :exit zero?))
  (apply shell {:err nil} "clj-kondo --lint" args))

(defn setup-git-hooks
  "Install required Git pre-commit hooks."
  []
  (spit ".git/hooks/pre-commit" "#!/usr/bin/env bb -m core/pre-commit\n")
  (shell "chmod +x .git/hooks/pre-commit")
  (println "Setup .git/hooks/pre-commit done."))

(defn- modified-files
  "Return a list of the files that are part of the current commit.

  Each item is a string with the file path."
  []
  (-> (shell {:out :string} "git diff --cached --name-only --diff-filter=ACMR")
      :out
      str/split-lines))

(defn- update-file-index
  "Add unstaged modifications to git, so they get to be part of the
  current commit."
  [path]
  (let [hash (-> (shell {:out :string} "git hash-object -w" path)
                 :out
                 str/trim)]
    (shell "git update-index --add --cacheinfo 100644" hash path)))

(defn- clojure-source?
  [path]
  (re-matches #"^.+?\.(clj|cljs|cljc)$" path))

(defn pre-commit
  [& _args]
  (when-let [paths (->> (modified-files)
                        (sequence (filter clojure-source?))
                        seq)]
    (doseq [path paths]
      (apply cljfmt ["fix" "--file-pattern" path])
      (update-file-index path))
    (apply clj-kondo paths)))
