(ns workshop.live
  (:require
   [nextjournal.clerk :as clerk]
   [aerial.hanami.common :as hc :refer [RMV]]
   [aerial.hanami.templates :as ht]))

(comment
  ;; just run these once each
  (clerk/serve! {:port 7777 :browse? true :watch-paths ["src/workshop"]})
  (clerk/show! "src/workshop/live.clj"))

(def co2-level
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

(def stacked-decade-chart
  (assoc ht/line-layer
         :encoding ht/xy-encoding
         ))

(clerk/vl co2-level)

(def trend-layer
  (assoc ht/line-chart
         ::ht/defaults
         {:X "Year"
          :XTYPE "temporal"
          :Y "Annual Mean Temperature"
          :YSCALE {:zero false}
          :WIDTH 500}))

(def trend-chart
  (assoc ht/layer-chart
         ::ht/defaults
         {:LAYER [(hc/xform trend-layer
                            :TRANSFORM [{:filter {:field "Geography" :equal "UK"}}])
                  (hc/xform trend-layer
                            :MCOLOR :trend-color
                            :TRANSFORM [{:loess :Y :on :X}])]
          :trend-color "firebrick"}))

(def hanami-graph
  (hc/xform trend-chart
            :FDATA "resources/data/annual-mean-temp.csv"
            :trend-color "orange"))

(clerk/vl hanami-graph)

(require '[clojure.data.csv :as csv])

(-> "resources/data/annual-mean-temp.csv"
    slurp
    csv/read-csv
    clerk/use-headers
    clerk/table)

(def point-chart
  (hc/xform ht/point-chart
            :UDATA "https://vega.github.io/vega-lite/data/cars.json"
            :X "Horsepower"
            :Y "Miles_per_Gallon"
            :COLOR "Origin"))

(clerk/vl point-chart)
