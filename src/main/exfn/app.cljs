(ns exfn.app
  (:require [reagent.dom :as dom]
            [re-frame.core :as rf]
            [exfn.subscriptions]
            [exfn.events]
            [goog.string :as gstring]
            [clojure.string :as str]
            [clojure.set :as set]))

<<<<<<< HEAD
;; -- Helpers -------------------------------------------------------
(defn check [letters words]
  (let [letters (set letters)]
    (->> words
         (filter (fn [w] (set/subset? (set w) letters)))
         (set))))

(defn keyed-collection [col]
  (map vector (iterate inc 0) col))

;;-- Events and Effects ---------------------------------------------
(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:words []
    :current-word ""
    :winners []
    :letters #{}}))

(rf/reg-event-db
 :toggle-letter
 (fn [{:keys [letters words] :as db} [_ letter]]
   (let [new-letters (cond (letters letter)
                           (set/difference letters #{letter})

                           (= 18 (count letters))
                           letters

                           :else
                           (conj letters letter))]
     (-> db
         (assoc :letters new-letters)
         (assoc :winners (check new-letters words))))))

(rf/reg-event-db
 :word-change
 (fn [db [_ word]]
   (assoc db :current-word word)))

(rf/reg-event-db
 :add-word
 (fn [{:keys [current-word letters words] :as db} _]
   (let [current-word (str/lower-case current-word)
         new-words (conj words current-word)]
     (-> db
         (update :words conj current-word)
         (assoc :current-word "")
         (assoc :winners (check letters new-words))))))

(rf/reg-event-db
 :delete
 (fn [{:keys [words] :as db} [_ word-to-remove]]
   (let [new-words (set (remove (fn [word] (= word-to-remove word)) words))]
     (-> db
         (assoc :words new-words)
         (assoc :winners (check (db :letters) new-words))))))

;; -- Subscriptions --------------------------------------------------
(rf/reg-sub
 :letters
 (fn [db _]
   (db :letters)))

(rf/reg-sub
 :current-word
 (fn [db _]
   (db :current-word)))

(rf/reg-sub
 :words
 (fn [db _]
   (db :words)))

(rf/reg-sub
 :winners
 (fn [db _]
   (db :winners)))

;; -- Reagent Forms --------------------------------------------------
(defn letters-row [letters selected-letters]
  [:div.flex-container
   (for [[k letter] (keyed-collection letters)]
     [:div.letter-box.pointable
      {:key      k
       :style    {:background-color (if (selected-letters letter)
                                      "#EE6C4D"
                                      "#f1f1f1")}
       :on-click #(rf/dispatch [:toggle-letter letter])}
      [:label.pointable.letter letter]])])

(defn letters []
  (let [selected-letters @(rf/subscribe [:letters])]
    [:div.letters
     [:h3 "Select upto 18 letters"]
     (let [[a-to-m n-to-z] (partition 13 "abcdefghijklmnopqrstuvwxyz")]
       [:div
        [letters-row a-to-m selected-letters]
        [letters-row n-to-z selected-letters]
        [:div.selected-count
         (str "Selected " (count selected-letters) " letters")]])]))

(defn word-editor []
  (let [winners @(rf/subscribe [:winners])
        winner? (>= (count winners) 3)
        current-word @(rf/subscribe [:current-word])]
    [:div
     [:h3 "Words"]
     [:div
      [:label "Enter word: "]
      [:input.word-input
       {:type      "text"
        :on-change #(rf/dispatch-sync [:word-change (-> % .-target .-value)])
        :value     @(rf/subscribe [:current-word])}]
      [:button.btn.btn-primary
       {:on-click #(rf/dispatch [:add-word])
        :disabled (= "" (str/trim current-word))}
       [:i.fas.fa-plus]]
      [:label.winners-count
       (str (count winners) " Winners!")]]
     [:h1.winner-indicator.winner
      {:style {:visibility (if winner? :visible :collapse)
               :height     (if winner? 40 0)}}
      "You are a winner!"]]))

(defn star [hidden?]
  [:i.fas.fa-star.star.winner-indicator
   {:style {:visibility (if hidden? :visible :hidden)}}])

(defn words []
  (let [words @(rf/subscribe [:words])
        winners @(rf/subscribe [:winners])]
=======
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
>>>>>>> 0153277fd7c5f22d1bc186c6cc29b686d2705ef6
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

<<<<<<< HEAD
;; -- App ------------------------------------------------------------
=======
>>>>>>> 0153277fd7c5f22d1bc186c6cc29b686d2705ef6
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