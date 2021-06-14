(ns exfn.app
  (:require [reagent.dom :as dom]
            [re-frame.core :as rf]
            [exfn.subscriptions]
            [exfn.events]
            [goog.string :as gstring]
            [clojure.string :as str]
            [clojure.set :as set]))

(defn bmi-widget [bmi]
  [:div.bmi
   [:div.row.bmi-container
    [:div.col.col-lg-2.bmi-underweight "15"]
    [:div.col.col-lg-4.bmi-normal "18.5"]
    [:div.col.col-lg-3.bmi-overweight "25"]
    [:div.col.col-lg-3.bmi-obese "30"]]
   [:div.indicator.bmi-indicator {:style {:left (str (* (/ (- bmi 15) 20.0) 100) "%")}}
    [:i.fas.fa-sort-up]]])


(defn get-current-weight-from-stats [days]
  (->> days
       (map (fn [{:keys [date weight]}]
              {:date   (js/Date. date)
               :weight weight}))
       (sort-by :date >)
       (first)
       :weight))

(defn calc-bmi [height weight]
  (/ weight (* height height)))

(defn weight-tracker []
  (let [{:keys [target-weight days]} @(rf/subscribe [:daily-stats])
        current-weight (get-current-weight-from-stats days)
        bmi (calc-bmi 1.75 current-weight)]
    [:div
     [:div.row
      [:div.col.col-lg-9]
      [:div.col.col-lg-3
       [:div.weight
        [:div.row.weight-row
         [:div.col.col-md-9  "Current Weight"]
         [:div.col.col-md-3.weight-value current-weight " kg"]]
        [:div.row.weight-row
         [:div.col.col-md-9 "Target Weight"]
         [:div.col.col-md-3.weight-value target-weight " kg"]]
        [:div.row.weight-row
         [:div.col.col-md-9 "To Lose"]
         [:div.col.col-md-3.weight-value (- current-weight target-weight) " kg"]]
        [:div.row
         [:div.col.col-md-9 "BMI"]
         [:div.col.col-md-3.weight-value (gstring/format "%.2f" bmi)]]
        [:div.row
         [:div.col.col-md-12
          [bmi-widget bmi]]]]]]]))

(defn app []
  [:div.container
   [:h1 "Fitness Tracker"]
   [weight-tracker]])

;; -- After-Load -----------------------------------------------------
;; Do this after the page has loaded.
;; Initialize the initial db state.
(defn ^:dev/after-load start
  []
  (dom/render [app]
              (.getElementById js/document "app")))

(defn ^:export init []
  (start))

(defonce initialize (rf/dispatch-sync [:initialize]))