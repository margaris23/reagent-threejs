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
(defn rotateBox [box]
  (set! (.. box -rotation -x) (+ (.. box -rotation -x) 0.005))
  (set! (.. box -rotation -y) (+ (.. box -rotation -y) 0.01)))

(defn box [color]
  (let [geometry (t/BoxGeometry. 1 1 1)
        material (t/MeshBasicMaterial. #js {:color color :wireframe true :transparent true})]
    (t/Mesh. geometry material)))

(defn MyScene []
  (let [scene (t/Scene.)
        camera (t/PerspectiveCamera. 75 (/ (.-innerWidth js/window) (.-innerHeight js/window)) 0.1 1000)
        renderer (t/WebGLRenderer.)
        cube (box 0x00ff00)
        cube2 (box 0xff0000)
        cube3 (box 0x0000ff)
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
          (rotateBox cube)
          (rotateBox cube2)
          (rotateBox cube3)
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
      ; add scene items
      (.add scene cube)
      (.add scene cube2)
      (.add scene cube3)
      (set! (.. cube2 -position -x) 2)
      (set! (.. cube2 -rotation -x) 0.5)
      (set! (.. cube3 -position -x) -2)
      (set! (.. cube3 -rotation -x) -0.5)
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
