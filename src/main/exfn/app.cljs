(ns exfn.app
  (:require [reagent.dom :as dom]
            [re-frame.core :as rf]
            [goog.string.format]
            [clojure.set :as set]))

;; -- Helpers ------------------------------------------------------------------------------------
(defn check [letters words]
  (let [letters (set letters)]
    (->> words
         (filter (fn [w] (set/subset? (set w) letters)))
         (set))))

(defn keyed-collection [col]
  (map vector (iterate inc 0) col))

;;-- Events and Effects --------------------------------------------------------------------------
(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:words #{}
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
   (let [new-words (conj words current-word)]
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

;; -- Subscriptions ------------------------------------------------------------------
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

;; -- Reagent Forms ------------------------------------------------------------------
(defn letters []
  (let [selected-letters @(rf/subscribe [:letters])]
    [:div {:style {:margin-bottom 20}}
     [:h3 "Select upto 18 letters"]
     (let [letters "abcdefghijklmnopqrstuvwxyz"
           a-to-m (->> letters
                       (take 13)
                       (map identity))
           n-to-z (->> letters
                       (drop 13)
                       (map identity))]
       [:div
        [:div.flex-container
         (for [[k letter] (keyed-collection a-to-m)]
           [:div {:key k
                  :style {:cursor           :pointer
                                   :background-color (if (selected-letters letter) :yellow "#f1f1f1")}
                  :on-click #(rf/dispatch [:toggle-letter letter])}
            [:label {:style {:cursor :pointer}} letter]])]
        [:div.flex-container
         (for [[k letter] (keyed-collection n-to-z)]
           [:div  {:key k
                   :style    {:cursor           :pointer
                              :background-color (if (selected-letters letter) :yellow "#f1f1f1")}
                   :on-click #(rf/dispatch [:toggle-letter letter])}
            [:label {:style {:cursor :pointer}} letter]])]
        [:div (str "Selected " (count selected-letters) " letters")]])]))

(defn word-editor []
  (let [winners @(rf/subscribe [:winners])]
    [:div
     [:h3 "Words"]
     [:div {:style {:visibility (if (>= (count winners) 3) :visible :collapse)
                    :height (if (>= (count winners) 3) 40 0)}}
      [:h1 {:style {:color :orange
                    :text-shadow "-1px 0 red, 0 1px red, 1px 0 red, 0 -1px red"}}
       "You are a winner!"]]
     [:div
      [:label "Enter word: "]
      [:input {:type "text"
               :on-change #(rf/dispatch-sync [:word-change (-> % .-target .-value)])
               :value @(rf/subscribe [:current-word])
               :style {:margin 10}}]
      [:button.btn.btn-primary
       {:on-click #(rf/dispatch [:add-word])}
       [:i.fas.fa-plus]]
      [:label.winners-count 
       (str (count winners) " Winners!")]]]))

(defn words []
  (let [words @(rf/subscribe [:words])
        winners @(rf/subscribe [:winners])]
    (js/console.log winners)
    [:div
     [:ul.no-bullets
      (for [[k w] (keyed-collection words)]
        [:li {:key k}
         [:div [:button.delete.btn
                {:on-click #(rf/dispatch [:delete w])}
                [:i.fas.fa-trash-alt]]
          [:i.fas.fa-star {:style {:color :orange
                                   :margin-right 5
                                   :text-shadow "-1px 0 red, 0 1px red, 1px 0 red, 0 -1px red"
                                   :visibility (if (winners w) :visible :hidden)}}]
          w
          [:i.fas.fa-star {:style {:color :orange
                                   :margin-left 5
                                   :text-shadow "-1px 0 red, 0 1px red, 1px 0 red, 0 -1px red"
                                   :visibility (if (winners w) :visible :hidden)}}]]])]]))

;; -- App -------------------------------------------------------------------------
(defn app []
  [:div.container
   [:h1 "Letters Lotto Checker"]
   [letters]
   [word-editor]
   [words]])

;; -- Dev Events --------------------------------------------------------------------
(rf/reg-event-db
 :clear
 (fn [db _]
   (assoc db :letters #{})))

(rf/reg-event-db
 :clear-words
 (fn [db _]
   (assoc db :words #{})))

(rf/reg-event-db
 :clear-winners
 (fn [db _]
   (assoc db :winners #{})))

(comment (rf/dispatch [:clear])
         (rf/dispatch [:clear-words])
         (rf/dispatch [:clear-winners]))

;; -- After-Load --------------------------------------------------------------------
;; Do this after the page has loaded.
;; Initialize the initial db state.
(defn ^:dev/after-load start
  []
  (dom/render [app]
              (.getElementById js/document "app")))

(defn ^:export init []
  (start))

(defonce initialize (rf/dispatch-sync [:initialize]))       ; dispatch the event which will create the initial state. 


