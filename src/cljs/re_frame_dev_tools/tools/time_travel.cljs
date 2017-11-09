(ns re-frame-dev-tools.tools.time-travel
  (:require [day8.re-frame.undo :as undo :refer [undoable]]
            [re-frame.core :as re-frame]))

(def ^:private
  time-travel-button-wrapper-styles
  {:margin "auto 5px"
   :width "100px"
   :overflow "hidden"})

(def ^:private
  time-travel-event-preview-styles
  {:font-size "0.7em"
   :margin "4px 0px"
   :line-height "8px"
   :font-family  "Courier New, Courier, Lucida Sans Typewriter, monospace"
   :text-align "center"
   :white-space "nowrap"
   :overflow "hidden"
   :text-overflow "ellipsis"})

(defn- time-travel-button
  [{:keys [on-click disabled event title]}]
  [:div
   {:class "time-travel-button-wrapper"
    :style time-travel-button-wrapper-styles}
   [:p
    {:class "time-travel-event-preview"
     :style time-travel-event-preview-styles}
    (if (empty? event) "-" event)]
   [:button
    (cond-> {:class "time-travel-button"
             :on-click on-click
             :disabled disabled})
    title]])

(def ^:private
  time-travel-wrapper-styles
  {:background-color "#BFBFBF"
   :padding "5px 10px"})

(def ^:private
  time-travel-control-styles
  {:display :flex})

(defn- time-travel-app
  []
  (let [undo-explanations (re-frame/subscribe [:undo-explanations])
        redo-explanations (re-frame/subscribe [:redo-explanations])
        undos? (re-frame/subscribe [:undos?])]
    (fn []
      (let [history-count (count @undo-explanations)
            future-count (count @redo-explanations)
            previous-event (last @undo-explanations)
            next-event (first @redo-explanations)]
        [:div {:style time-travel-wrapper-styles}
         [:div {:class "time-tavel-controls"
                :style time-travel-control-styles}

          [time-travel-button {:on-click #(re-frame/dispatch [:undo])
                               :disabled (not @undos?)
                               :title "< Previous"
                               :event previous-event}]

          [time-travel-button {:on-click #(re-frame/dispatch [:redo])
                               :disabled (empty? @redo-explanations)
                               :title "Next >"
                               :event next-event}]

          [time-travel-button {:on-click #(re-frame/dispatch [:purge-redos])
                               :disabled (empty? @redo-explanations)
                               :title "Reset Start"
                               :event "-"}]]]))))

(def time-travel-tool
  {:tool-name "Time Travel"
   :component time-travel-app
   :key :time-travel})

(def time-travel-interceptor
  (re-frame/->interceptor
   :id :time-travel
   :before (fn [context]
             (let [event-name (get-in context [:coeffects :event 0])]
               (update context :queue conj (undoable (name event-name)))))))
