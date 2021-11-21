;; # Welcome to the workshop on visualizing data with Hanami!

;; I'm Kira. I work with a UK-based company called Swirrl building open data publishing tools.

;; ## Intro

;; **Who is this for?**

;; People interested in data visualization but new to Hanami. We'll assume some familiarity with
;; Clojure and a working Clojure development environment, but no experience with any of the tools
;; or libraries in this workshop. Specifically I'll assume you already have Clojure and a Clojure
;; IDE set up on your computer.

;; **Outcomes**

;; By the end if this workshop, the goal is for everyone to be able to create some basic data
;; visualizations of arbitrary datasets using Hanami.

;; ## Outline

;;   1. Quick background -- What is Vega-Lite (VL)? Also Clerk
;;   2. Basics of Hanami -- How does it work? What does it do? What is its connection to VL?
;;   3. Practice together -- Exploring some different kinds of data visualization with Hanami

;; ## Quick Background

;; ### Clerk

;; _https://github.com/nextjournal/clerk_

;; Turns a clojure namespace into a "notebook", an HTML page you can render in your browser.

;; It also includes all kinds of helpful utilities for a seamless data exploration experience,
;; like this filewatcher and server, and renderers for various different special kinds of data,
;; including Vega-Lite specifications, which we'll make use of today.


;; ### ✅ Set up a clerk notebook on your machine!

;; ➡️ Make a new Clojure project with a `deps.edn` file with these contents:

;; ```clj
;; {:paths ["src" "resources"]
;;  :deps {org.clojure/clojure {:mvn/version "1.10.3"}
;;         aerial.hanami/aerial.hanami {:mvn/version "0.15.1"}
;;         io.github.nextjournal/clerk {:mvn/version "0.3.233"}}}
;; ```


;; _If you like you can copy a skeleton setup from here:_

;; _https://github.com/kiramclean/hanami-workshop/tree/main_

;; ➡️ Create a new `hanami.clj` namespace in `src/workshop/` with these contents:

(ns workshop.hanami
  (:require
   [nextjournal.clerk :as clerk]
   [aerial.hanami.common :as hc :refer [RMV]]
   [aerial.hanami.templates :as ht]))

;; ➡️ Start a repl, switch into the `workshop.hanami` namespace

;; ➡️ Start the clerk web server and file watcher:

(comment
  ;; just run these once each
  (clerk/serve! {:browse? true :watch-paths ["src"]})
  (clerk/show! "src/workshop/hanami.clj"))

;; ### Ok more background... Vega-Lite

;; A "grammar for graphics", sort of like a language but instead of describing arbitrary
;; instructions for a computer to execute, it describes data visualizations (using JSON).

;; A declarative way to describe how to visually encode data and interactions into a format
;; that can be rendered in a browser. Can think of it like any other language, made up of
;; words and rules for combining those words.

;; | "word" | description |
;; | -- | -- |
;; | `data` | input for the visualisation |
;; | `mark` | shape/type of graphics to use |
;; | `encoding` | mapping between data and marks |
;; | `transform` | e.g. filter, aggregate, bin, etc. |
;; | `scale` | meta-info about how the data should fit into the visualisation |
;; | `guides ` | legends, labels |

;; **"rules"**

;; `concat, layer, repeat, facet, resolve`

;; Much more to VL, but this is enough to get started.

;;  _https://vega.github.io/vega-lite/docs/_

;; #### Example

;; This is just a vega-lite spec cribbed directly from the [examples in their documentation](https://vega.github.io/vega-lite/examples/layer_line_co2_concentration.html)
;; and converted to `edn`:

(def vl-co2-concentration-spec
  {:data {:url "https://vega.github.io/vega-lite/data/co2-concentration.csv"}
   :width 700
   :height 500
   :transform [{:calculate "year(datum.Date)" :as "year"}
               {:calculate "floor(datum.year / 10)" :as "decade"}
               {:calculate "(datum.year % 10) + (month(datum.Date)/12)" :as "scaled_date"}]
   :encoding {:x {:type "quantitative" :title "Year into Decade" :axis {:tickCount 11}}
              :y {:title "CO2 concentration in ppm" :type "quantitative" :scale {:zero false}}
              :color {:field "decade" :scale {:scheme "magma"}}}
   :layer [{:mark "line" :encoding {:x {:field "scaled_date"} :y {:field "CO2"}}}
           {:mark {:type "text" :baseline "top"}
            :encoding {:x {:aggregate "min" :field "scaled_date"}
                       :y {:aggregate {:argmin "scaled_date"} :field "CO2"}
                       :text {:aggregate {:argmin "scaled_date"} :field "year"}}}]
   :config {:text {:align "left" :dx 3 :dy 1}}})

(clerk/vl vl-co2-concentration-spec)

;; This one is kind of elaborate, but just here to show how these components of the grammar
;; come together to make a Vega-Lite "sentence".

;; ## The Main Event - Hanami!

;; - Clojure wrapper for Vega-Lite
;; - Another level of abstraction/simplification on top of Vega-Lite in Clojure
;; - Declarative, composable, recursively parameterized visualisation templates

;; **Templates** are maps of **substitution keys**

;; Several **default** substitution keys and templates are defined by the library
;; already to use as starting points

@hc/_defaults

;; To inspect an individual default value:

(hc/get-default :BACKGROUND)

;; ### Key functions:

;; `hc/xform`, `hc/get-default`, `ht/view-base`, `ht/<chart-type>`

;; #### Explanation

;; `hc/xform` is the main/general transformation function. Takes a template and optionally
;; a series of extra transformation key/value pairs. E.g. to substitute
;; `:BACKGROUND` in a custom map with its default value:

(hc/xform {:my-key :BACKGROUND})

;; Transformations are recursive. E.g. inspect the default value for `:TOOLTIP`:

(hc/get-default :TOOLTIP)

;; It's definition include several values which are themselves substitution keys.

;; When used in a transformation, all are replaced:

(hc/xform {:my-key :TOOLTIP})

;; See what happens when we supply a custom value to override a default one:

(hc/xform {:my-key :TOOLTIP} :X "my custom x value")

;; Good time to note the special `RMV` value:

(hc/get-default :XTTITLE)

RMV

;; Deletes/dissocs the key from the resulting map altogether, facilitates making
;; valid VL specs

;; ### Built-in templates

;; Skeleton base template:

ht/view-base

(hc/xform ht/view-base)

;; This is still not quite a valid VL spec. See, nothing:

(clerk/vl (hc/xform ht/view-base))

;; Hanami supplies several common chart templates and other useful composable
;; template pieces

ht/point-chart

ht/bar-chart

(def point-chart
  (hc/xform ht/point-chart
            :UDATA "https://vega.github.io/vega-lite/data/cars.json"
            :X "Horsepower"
            :Y "Miles_per_Gallon"
            :COLOR "Origin"))

(clerk/vl point-chart)

;; ### Data sources

;; Vega-Lite expects tabular data. Lots of details here: https://vega.github.io/vega-lite/docs/data.html

;; Hanami supports multiple ways of specifying the data source

;; - `:DATA` - expects an explicit vector of maps, where each map has the same keys
;; (the "header" row) pointing to the values
;; - `:UDATA` - expects a relative URL (for example "data/cars.json") or a fully
;; qualified URL to a csv or json data file
;; - `:NDATA` - expects a named Vega data channel
;; - `:FDATA` - expects the path to a `clj`, `edn`, `json`, or `csv` file.
;; Data in `clj`, `json`, or `edn` files must be a vector of maps. Data in `csv` files is
;; automatically converted to a vector of maps by hanami.

;; Better and more explanation here: https://github.com/jsa-aerial/hanami#data-sources

;; ## Examples!

;; #### Sidenote - data sources

;; https://climate-change.data.gov.uk

;; https://beta.gss-data.org.uk/datasets

;; https://www.kaggle.com/datasets

;; ### Hanami chart

(def hanami-chart
  (hc/xform ht/layer-chart
            :FDATA "resources/data/annual-mean-temp.csv"
            :LAYER
            [(hc/xform ht/line-chart
                       :X "Year"
                       :XTYPE "temporal"
                       :Y "Annual Mean Temperature"
                       :YSCALE {:zero false}
                       :TRANSFORM [{:filter {:field "Geography" :equal "UK"}}]
                       :WIDTH 700)
             (hc/xform ht/line-chart
                       :MCOLOR "firebrick"
                       :X "Year"
                       :XTYPE "temporal"
                       :Y "Annual Mean Temperature"
                       :YSCALE {:zero false}
                       :TRANSFORM [{:filter {:field "Geography" :equal "UK"}}
                                   {:loess :Y :on :X}]
                       :WIDTH 700)]))

(-> hanami-chart clerk/vl)

;; - To remove zero as the baseline: https://vega.github.io/vega-lite/docs/scale.html#continuous-scales
;; - Details about specifying transformations: https://vega.github.io/vega-lite/docs/transform.html
;; - Loess transformation: https://vega.github.io/vega-lite/docs/loess.html

;; ### Other side note - Clerk tabular data inspection

(require '[clojure.java.io :as io])
(require '[clojure.data.csv :as csv])

(-> "resources/data/annual-mean-temp.csv"
    slurp
    csv/read-csv
    clerk/use-headers
    clerk/table)

;; ### Template-local defaults

(def trend-layer
  (assoc ht/line-layer
         ::ht/defaults {:X "Year"
                        :XTYPE "temporal"
                        :Y "Annual Mean Temperature"
                        :YSCALE {:zero false}
                        :TRANSFORM [{:filter {:field "Geography" :equal "UK"}}]
                        :WIDTH 700}))

(def trend-chart
  (assoc ht/layer-chart
         ::ht/defaults
         {:LAYER
          [(hc/xform trend-layer)
           (hc/xform trend-layer
                     :MCOLOR :trend-color
                     :TRANSFORM [{:loess :Y :on :X}])]
          :trend-color "firebrick"}))

(def custom-trend-chart
  (hc/xform trend-chart
            :FDATA "resources/data/annual-mean-temp.csv"
            :trend-color "orange"))

(-> custom-trend-chart clerk/vl)

;; ### Hanamifying the example from earlier

(def year-labels-layer
  {:mark {:type "text" :baseline "top"}
   :encoding (assoc ht/xy-encoding
                    :text {:aggregate :TXTAGG :field :TXT}
                    ::ht/defaults {:TOOLTIP RMV})})

(def stacked-decade-chart
  (assoc ht/layer-chart
         :encoding ht/xy-encoding
         :config {:text {:dx 3 :dy 1 :align "left"}}
         ::ht/defaults {:XAXIS {:tickCount 11}
                        :TRANSFORM [{:calculate "year(datum.Date)" :as "year"}
                                    {:calculate "floor(datum.year / 10)" :as "decade"}
                                    {:calculate "(datum.year % 10) + (month(datum.Date)/12)" :as "scaled_date"}]
                        :YSCALE {:zero false}
                        :COLOR {:field "decade" :scale {:scheme "magma"}}}))

(def hanami-co2-concentration-chart
  (hc/xform stacked-decade-chart
            :UDATA "https://vega.github.io/vega-lite/data/co2-concentration.csv"
            :WIDTH 700
            :HEIGHT 500
            :BACKGROUND "white"
            :LAYER [(hc/xform ht/line-layer
                              :X "scaled_date"
                              :Y "CO2"
                              :XTITLE "Year into Decade"
                              :YTITLE "CO2 concentration in ppm")
                    (hc/xform year-labels-layer
                              :X "scaled_date"
                              :XAGG "min"
                              :Y "CO2"
                              :YAGG {:argmin "scaled_date"}
                              :TXT "year"
                              :TXTAGG {:argmin "scaled_date"})]))

(clerk/vl hanami-co2-concentration-chart)
