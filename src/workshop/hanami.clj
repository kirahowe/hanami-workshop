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
