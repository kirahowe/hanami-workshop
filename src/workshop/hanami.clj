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

;;   1. Quick background -- What is Vega-Lite (VL)? Hanami? Clerk?
;;   2. Basics of Hanami -- How does it work? What does it do? What is its connection to VL?
;;   3. Practice together -- Exploring some different kinds of data visualization with Hanami

;; Headers that start with emojis are instructions to pay attention to and/or do yourself.

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
;;         io.github.nextjournal/clerk {:mvn/version "0.2.214"}}}
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
   :width 800
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

;; ## The Main Event - Hanami!

;; ### Sidenote - data sources

;; https://climate-change.data.gov.uk
;; https://beta.gss-data.org.uk/datasets


(def hanami-graph
  (hc/xform
   ht/layer-chart
   :FDATA "https://beta.gss-data.org.uk/download/zip-contents/live/341c08c7-9afa-5667-bd5e-d07d6d47ff1c/gss-data-org-uk-annual-mean-temp-with-trends-actual.zip/gss-data-org-uk-annual-mean-temp-with-trends-actual/data.csv"
   :LAYER
   [(hc/xform
     ht/line-chart
     :WIDTH 700
     :XTYPE "temporal"
     :X "Year"
     :Y "Annual Mean Temperature"
     :YSCALE {:zero false}
     :TRANSFORM [{:filter {:field "Geography" :equal "UK"}}])
    (hc/xform
     ht/line-chart
     :TRANSFORM [{:filter {:field "Geography" :equal "UK"}}
                 {:loess :Y :on :X}]
     :WIDTH 700
     :MCOLOR "firebrick"
     :X "Year"
     :XTYPE "temporal"
     :Y "Annual Mean Temperature"
     :YSCALE {:zero false})]))

(-> hanami-graph clerk/vl)

;; (clerk/vl  (hc/xform
;;             ht/layer-chart
;;             :FID :f2 :VID :v2
;;            :UDATA "https://vega.github.io/vega-lite/data/cars.json"
;;            :LAYER [;; (hc/xform
;;                    ;;  ht/point-chart :FID :f2 :VID :v2 :OPACITY 0.3
;;                    ;;  :X "Horsepower" :Y "Miles_per_Gallon" :COLOR "Origin")
;;                    (hc/xform
;;                     ht/line-chart :XBIN true :YAGG :mean
;;                     :X "Horsepower" :Y "Miles_per_Gallon" :COLOR "Origin")])
;;           )


;; (def vl-choropleth-spec
;;   {:width 500,
;;    :height 300,
;;    :data
;;    {:url "https://vega.github.io/vega-lite/data/us-10m.json",
;;     :format {:type "topojson", :feature "counties"}},
;;    :transform
;;    [{:lookup "id",
;;      :from
;;      {:data {:url "https://vega.github.io/vega-lite/data/unemployment.tsv"},
;;       :key "id",
;;       :fields ["rate"]}}],
;;    :projection {:type "albersUsa"},
;;    :mark "geoshape",
;;    :encoding {:color {:field "rate", :type "quantitative"}}})

;; (-> vl-choropleth-spec clerk/vl)

;; (def vl-repeat-spec
;;   {:repeat
;;    ["Horsepower" "Miles_per_Gallon" "Acceleration" "Displacement"],
;;    :columns 2,
;;    :spec
;;    {:data {:url "https://vega.github.io/vega-lite/data/cars.json"},
;;     :mark "bar",
;;     :encoding
;;     {:x {:field {:repeat "repeat"}, :bin true},
;;      :y {:aggregate "count"},
;;      :color {:field "Origin"}}}})

;; (-> vl-repeat-spec clerk/vl)

;; (def hanami-repeat
;;   (hc/xform ht/bar-chart
;;             :UDATA "https://vega.github.io/vega-lite/data/cars.json"
;;             :X "Horsepower"
;;             :YAGG "count"))

;; (-> hanami-repeat clerk/vl)
;; (def vl-time-series-spec
;;   {:transform [{:filter "datum.symbol !== 'GOOG'"}],
;;    :width 300,
;;    :height 100,
;;    :data {:url "https://vega.github.io/vega-lite/data/stocks.csv"},
;;    :mark "rect",
;;    :encoding
;;    {:x
;;     {:timeUnit "yearmonthdate",
;;      :field "date",
;;      :type "ordinal",
;;      :title "Time",
;;      :axis
;;      {:format "%Y",
;;       :labelAngle 0,
;;       :labelOverlap false,
;;       :labelColor
;;       {:condition
;;        {:test
;;         {:timeUnit "monthdate",
;;          :field "value",
;;          :equal {:month 1, :date 1}},
;;         :value "black"},
;;        :value nil},
;;       :tickColor
;;       {:condition
;;        {:test
;;         {:timeUnit "monthdate",
;;          :field "value",
;;          :equal {:month 1, :date 1}},
;;         :value "black"},
;;        :value nil}}},
;;     :color
;;     {:aggregate "sum",
;;      :field "price",
;;      :type "quantitative",
;;      :title "Price"},
;;     :y {:field "symbol", :type "nominal", :title nil}}})

;; (-> vl-time-series-spec clerk/vl)
