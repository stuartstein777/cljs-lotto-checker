(ns exfn.subscriptions
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :daily-stats
 (fn [db _]
   (db :daily-stats)))