(ns music-scrobbler.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 :api-key
 (fn [db]
   (:api-key db)))

(re-frame/reg-sub
 :secret-key
 (fn [db]
   (:secret-key db)))

(re-frame/reg-sub
 :token
 (fn [db]
   (:token db)))

(re-frame/reg-sub
 :api-sig
 (fn [db]
   (:api-sig db)))

(re-frame/reg-sub-raw
 :username
 (fn [db]
   (reaction (get-in @db [:get-session-result :session :name]))))

(re-frame/reg-sub-raw
 :session-key
 (fn [db]
   (reaction (get-in @db [:get-session-result :session :key]))))

(re-frame/reg-sub
 :api-result
 (fn [db]
   (:api-result db)))

(re-frame/reg-sub
 :get-session-result
 (fn [db]
   (:get-session-result db)))

(re-frame/reg-sub
 :recent-scrobbled-track
 (fn [db]
   (:recent-scrobbled-track db)))

(re-frame/reg-sub-raw
 :user-recent-tracks-list
 (fn [db]
   (reaction (get-in @db [:api-result :recenttracks :track]))))

;; track
(re-frame/reg-sub
 :artist
 (fn [db]
   (:artist db)))

(re-frame/reg-sub
 :track
 (fn [db]
   (:track db)))
