(ns re-frame-dev-tools.core
  (:require [reagent.core :as reagent]

            [re-frame-dev-tools.tools.time-travel :refer [time-travel-tool]]))

(defonce tool-state
  (reagent/atom {:active-tool nil
                 :app-closed false
                 :drawer-open false}))

(defn- toggle-drawer-state!
  "Open and close the dev tools window"
  [_]
  (swap! tool-state update :drawer-open not))

(defn- set-active-tool!
  "Sets an active tool"
  [tool-key]
  (swap! tool-state assoc :active-tool tool-key)
  (toggle-drawer-state! nil))

(defn- close-app!
  "Hides the dev tools"
  []
  (swap! tool-state assoc :app-closed true))

(defn- append-dev-tools-node!
  "Adds a mounting node to the dom for the cljs-dev-tools app"
  []
  (let [dev-tools-node
        (-> js/document
            (.createElement "div"))]

    ;; Add id for mounting purposes
    (set! (.-id dev-tools-node) "cljs-dev-tools")

    (-> js/document
        (.getElementsByTagName "body")
        (.item 0)
        (.appendChild dev-tools-node))))

(def ^:private widget-list-styles
  {:list-style "none"
   :padding-left "0"})

(defn- render-tool-list
  [widgets]
  [:div.open-drawer
   [:button {:on-click toggle-drawer-state!} "<-"]
   [:div
    [:h4 "CLJS Dev Tools"]
    [:hr]
    [:ul
     {:class "widget-list"
      :style widget-list-styles}
     (for [{:keys [key tool-name]} widgets]
       ^{:key key}
       [:li [:button {:on-click #(set-active-tool! key)} tool-name]])
     [:li [:button {:on-click close-app!} "Close"]]]]])

(def ^:private tool-wrapper-styles
  {:display "flex"
   :align-items "flex-end"})

(def ^:private draw-toggle-button-styles
  {:display "block"
   :height "20px"})

(defn- render-tool
  [widgets]
  (let [{:keys [active-tool]}
        @tool-state

        {:keys [component]}
        (first (filter #(= (:active-tool @tool-state)
                           (:key %))
                       widgets))]
    [:div
     {:class "tool-wrapper"
      :style tool-wrapper-styles}
     [:button
      {:on-click toggle-drawer-state!
       :style draw-toggle-button-styles} "->"]
     (when component
       [component])]))

(def ^:private dev-tool-wrapper-styles
  {:position "fixed"
   :bottom "0"
   :left "0"
   :background-color "#C3C3C3"
   :padding "4px"})

(defn- cljs-dev-tools-app
  [{:keys [enable-time-travel]
    :or {enable-time-travel true}
    :as options}]
  (let [widgets
        (remove nil? [(when enable-time-travel time-travel-tool)
                      {:key :reset
                       :tool-name "None"
                       :component nil}])]
    (fn []
      (when-not (:app-closed @tool-state)
        [:div {:class "cljs-dev-tool-wrapper"
               :style dev-tool-wrapper-styles}
         (if (:drawer-open @tool-state)
           [render-tool-list widgets]
           [render-tool widgets])]))))

(defn enable-dev-tools!
  ([]
   (enable-dev-tools! {}))
  ([options]

   (js/console.log "[CLJS DevTool]: enabled")

   (append-dev-tools-node!)

   (reagent/render [cljs-dev-tools-app options]
                   (js/document.getElementById "cljs-dev-tools"))))
