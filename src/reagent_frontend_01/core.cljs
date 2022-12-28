(ns reagent-frontend-01.core
    (:require
      [reagent.core :as r]
      [reagent.dom :as d]
      ["react" :as react]
      [three :as t]
      ["three/examples/jsm/controls/FlyControls" :as tfc]))

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

(defn box []
  (let [geometry (t/BoxGeometry. 1 1 1)
        material (t/MeshBasicMaterial. #js {:color 0x00ff00 :wireframe true :transparent true})]
    (t/Mesh. geometry material)))

(defn MyScene []
  (let [scene (t/Scene.)
        camera (t/PerspectiveCamera. 75 (/ (.-innerWidth js/window) (.-innerHeight js/window)) 0.1 1000)
        renderer (t/WebGLRenderer.)
        cube (box)
        clock (t/Clock.)
        controls (tfc/FlyControls. camera (.-domElement renderer))
        !my-ref (react/useRef)]
    ; window resize function
    (defn onWindowResize []
      (set! (.-aspect camera) (/ (.-innerWidth js/window) (.-innerHeight js/window)))
      (.updateProjectionMatrix camera)
      (.setSize renderer (.-innerWidth js/window) (.-innerHeight js/window)))
    ; custom animation function
    (defn animate []
      (let [delta (.getDelta clock)]
        (do
          (set! (.. cube -rotation -x) (+ (.. cube -rotation -x) 0.005))
          (set! (.. cube -rotation -y) (+ (.. cube -rotation -y) 0.01))
          (.update controls delta)
          (.render renderer scene camera)
          (set! (.-current !my-ref) (js/requestAnimationFrame animate)))))
    ; effect runs only once
    (react/useEffect (fn []
       (do
         (set! (.-current !my-ref) (js/requestAnimationFrame animate))
         #(js/cancelAnimationFrame (.-current !my-ref))))
       (array)) ; array is used to run effect only once
    ; steps to generate result
    (do
      (.setPixelRatio renderer (.-devicePixelRatio js/window)) ; possibly not needed
      (.setSize renderer (.-innerWidth js/window) (.-innerHeight js/window))
      (.appendChild (.-body js/document) (.-domElement renderer))
      (.add scene cube)
      ; fly controls
      (set! (.-movementSpeed controls) 5)
      (set! (.-domElement controls) (.-domElement renderer))
      (set! (.-autoForward controls) false)
      (set! (.-dragToLook controls) false)
      ; rest of actions
      (set! (.. camera -position -z) 5)
      (.addEventListener js/window "resize" onWindowResize)
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
