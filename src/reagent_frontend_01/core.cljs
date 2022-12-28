(ns reagent-frontend-01.core
    (:require
      [reagent.core :as r]
      [reagent.dom :as d]
      ["react" :as react]
      [three :as t]))

;; -------------------------
;; Components

; (defn test-component []
;   (let [[counter set-counter] (react/useState 0)]
;     (react/useEffect #(do 
;                         (.log js/console "Ok")
;                         (set! (.-title js/document) counter)))
;     (r/as-element
;       [:div
;         [:p (str "counter is " counter)]
;         [:button {:on-click #(set-counter (inc counter))} "+"]
;         [:button {:on-click #(set-counter (dec counter))} "-"]])))

(defn MyScene []
  (let [scene (t/Scene.)
        camera (t/PerspectiveCamera. 75 (/ (.-innerWidth js/window) (.-innerHeight js/window)) 0.1 1000)
        renderer (t/WebGLRenderer.)
        geometry (t/BoxGeometry. 1 1 1)
        material (t/MeshBasicMaterial. #js {:color 0x00ff00 :wireframe true})
        cube (t/Mesh. geometry material)
        !my-ref (react/useRef)]
    ; custom animation function
    (defn animate []
      (do
        (set! (.. cube -rotation -x) (+ (.. cube -rotation -x) 0.01))
        (set! (.. cube -rotation -y) (+ (.. cube -rotation -y) 0.01))
        (.render renderer scene camera)
        (set! (.-current !my-ref) (js/requestAnimationFrame animate))))
    ; effect runs only once
    (react/useEffect (fn []
       (do
         (set! (.-current !my-ref) (js/requestAnimationFrame animate))
         #(js/cancelAnimationFrame (.-current !my-ref))))
       (array)) ; array is used to run effect only once
    ; steps to generate result
    (do
      (.setSize renderer (.-innerWidth js/window) (.-innerHeight js/window))
      (.appendChild (.-body js/document) (.-domElement renderer))
      (.add scene cube)
      (set! (.. camera -position -z) 5)
      ; necessary to convert to JSX
      (r/as-element [:<>]))))

(comment
  (r/create-element [:p "test"])
  #_(r/create-element [:> test-component]))

;; -------------------------
;; Views

(defn home-page []
  [:main 
   [:> MyScene]
   ; [:> test-component]
   ])


;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
