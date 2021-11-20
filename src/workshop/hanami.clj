;; # My Hanami workspace!

(ns workshop.hanami
  (:require
   [nextjournal.clerk :as clerk]
   [aerial.hanami.common :as hc :refer [RMV]]
   [aerial.hanami.templates :as ht]))

;; ## Setup
;; ### Start a clerk web server and file watcher

(comment
  (clerk/serve! {:browse? true :watch-paths ["src"]})
  (clerk/show! "src/workshop/hanami.clj"))
