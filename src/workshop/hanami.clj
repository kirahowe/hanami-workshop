(ns workshop.hanami
  (:require
   [nextjournal.clerk :as clerk]
   [nextjournal.clerk.webserver :as clerk-webserver]
   [nextjournal.beholder :as beholder]
   [clojure.java.browse :as browse]
   [aerial.hanami.common :as hc :refer [RMV]]
   [aerial.hanami.templates :as ht]))

;; # Welcome to the workshop about Hanami!

;; ## Setup
;; ### Start a clerk web server and file watcher

(comment
  (def port 7777)
  (clerk-webserver/start! {:port port})
  (def filewatcher
    (beholder/watch #(clerk/file-event %) "src"))
  (browse/browse-url (str "http://localhost:" port))
  (clerk/show! "src/workshop/hanami.clj"))

;; For later, to execute when we want to stop the filewatcher
(comment
  (beholder/stop filewatcher))
