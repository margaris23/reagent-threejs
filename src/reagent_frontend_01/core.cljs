(ns reagent-frontend-01.core
    (:require
      [reagent.core :as r]
      [reagent.dom :as d]
      ["react" :as react]
      [three :as t]
      ["three/examples/jsm/controls/OrbitControls" :as toc]
      ["three/examples/jsm/controls/FlyControls" :as tfc]))

#_(set! *warn-on-infer* false)

;; Helper functions
(defn box [color]
  (let [geometry (t/BoxGeometry. 1 1 1)
        material (t/MeshBasicMaterial.
                    #js {:color color
                         :wireframe true
                         :transparent true})]
    (t/Mesh. geometry material)))

(defn rotateBox [box]
  (set! (.. box -rotation -x) (+ (.. box -rotation -x) 0.001))
  (set! (.. box -rotation -y) (+ (.. box -rotation -y) 0.005)))

(defn screen-ratio []
  (/ (.-innerWidth js/window) (.-innerHeight js/window)))

(defn formula [a b]
  (-> (/ a b) (* 2) (- 1)))

(defn onPointerMove [pointer]
  (fn [event]
    (set! (.-x pointer) (formula (.-clientX event) (.-innerWidth js/window)))
    (set! (.-y pointer) (formula (.-clientY event) (.-innerHeight js/window)))))

(defn onWindowResize [renderer ^js camera]
  (fn []
    (set! (.-aspect camera) (screen-ratio))
    (.updateProjectionMatrix camera)
    (.setSize renderer (.-innerWidth js/window) (.-innerHeight js/window))))

(def intersected (atom nil))

(defn update-intersection! [intersects]
  #_(js/console.log (.-length intersects))
  #_(js/console.log pointer)
  (if (> (.-length intersects) 0)
    ; if-case
    (when (not (= intersected (.-object (aget intersects 0))))
      (when-let [^js intersected @intersected]
        (set! (.. intersected -material -color) (.. intersected -currentColor)))
      (reset! intersected (.-object (aget intersects 0)))
      (set! (.-currentColor ^js @intersected) (.. @intersected -material -color))
      (set! (.. ^js @intersected -material -color) (t/Color. 0x777777)))
    ; else-case
    (do
      (when @intersected
        (set! (.. @intersected -material -color) (.-currentColor ^js @intersected)))
      (reset! intersected nil))))

;; -------------------------
;; Components

(defn MyScene []
  (let [scene (t/Scene.)
        camera (t/PerspectiveCamera. 75 (screen-ratio) 0.1 1000)
        renderer (t/WebGLRenderer.)
        cube (box 0x00ff00)
        cube2 (box 0xff0000)
        cube3 (box 0x0000ff)
        clock (t/Clock.)
        raycaster (t/Raycaster.)
        pointer (t/Vector2.)
        controls (toc/OrbitControls. camera (.-domElement renderer))
        ; controls (tfc/FlyControls. camera (.-domElement renderer))
        !my-ref (react/useRef)]
    ; custom animation function
    (defn animate []
      (let [delta (.getDelta clock)]
        (rotateBox cube)
        (rotateBox cube2)
        (rotateBox cube3)
        (.update controls delta)
        (.setFromCamera raycaster pointer camera)
        (update-intersection! (.intersectObjects raycaster (.-children scene) false))
        (.render renderer scene camera)
        (set! (.-current !my-ref) (js/requestAnimationFrame animate))))
    ; effect (hook) runs only once
    (react/useEffect (fn []
                       (set! (.-current !my-ref) (js/requestAnimationFrame animate))
                       #(js/cancelAnimationFrame (.-current !my-ref)))
                     (array)) ; array is used to run effect only once
    ; steps to generate result
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
    ; (set! (.-movementSpeed controls) 5)
    ; (set! (.-domElement controls) (.-domElement renderer))
    ; (set! (.-autoForward controls) false)
    ; (set! (.-dragToLook controls) false)
    ; orbit controls
    (.set (.-target controls) 0 0 0)
    ; rest of actions
    (set! (.. camera -position -z) 5)
    (.addEventListener js/window "resize" (onWindowResize renderer camera))
    (.addEventListener js/window "mousemove" (onPointerMove pointer))
    ; necessary to convert to JSX
    (r/as-element [:<>])))

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
