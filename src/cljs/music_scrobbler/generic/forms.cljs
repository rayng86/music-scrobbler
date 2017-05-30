(ns music-scrobbler.generic.forms
  (:require [reagent.core :as r]
            [re-frame.core :as re-frame]
            [clojure.string :as string]))

(defn input-text
  [label value fallback-value set-change disabled?]
  [:div
   [:div [:label label]]
   [:div [:input
          {:type "text"
           :id (string/lower-case (string/join "-" (string/split label #"\s")))
           :class "text-form"
           :value @value
           :placeholder fallback-value
           :disabled disabled?
           :on-change set-change}]]])

(defn button
  [color label set-change disabled?]
   [:button { :id (string/lower-case (string/join "-" (string/split label #"\s")))
              :class color
              :disabled disabled?
              :on-click set-change} label])
