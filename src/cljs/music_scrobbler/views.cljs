(ns music-scrobbler.views
    (:require [re-frame.core :as re-frame]
              [clojure.string :as string]
              [cemerick.url :as url]
              [music-scrobbler.utils :refer [keyword-this]]
              [music-scrobbler.generic.forms :refer [input-text button]]))

(defn recent-tracks-panel
  [coll track artist]
  [:div#recent-tracks-panel
   (doall
    (map-indexed
     (fn [idx v]
       ^{:key idx}
       [:div.track
        [:label "Artist: "]
        [:span (get-in (artist v) [:#text])]
        [:label "Track: "]
        [:span (track v)]
        [:hr]])
      coll))])

(defn recent-tracks-component []
 (let [user-recent-tracks-list (re-frame/subscribe [:user-recent-tracks-list])]
   (fn []
     [:div
      [:div
       [:h2 "Recent Scrobbled Tracks"]
       [recent-tracks-panel
        @user-recent-tracks-list :name :artist]]])))


(defn main-panel []
  (let [name (re-frame/subscribe [:name])]
    (fn []
      [:div "Hello from " @name])))
