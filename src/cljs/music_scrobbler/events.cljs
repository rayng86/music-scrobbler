(ns music-scrobbler.events
    (:require [re-frame.core :as re-frame]
              [music-scrobbler.db :as db]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))
