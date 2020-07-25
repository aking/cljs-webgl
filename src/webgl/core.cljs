(ns webgl.core
  (:require
   [clojure.core.async :refer [chan put! go go-loop]]
   [goog.dom :as dom]
   [goog.object :as gobj]
   [twgl]
   [webgl.util :as util]))

;;------------------------------------------------------------------------;;
(defonce info (util/make-logger :info "webgl.core"))
(defonce warn (util/make-logger :warn "webgl.core"))
;;------------------------------------------------------------------------;;

(def +pos+
  ;;----------------------------------------------------------------------;;
  #js [-1, -1, 0, 1, -1, 0, -1, 1, 0, -1, 1, 0, 1, -1, 0, 1, 1, 0])

(defonce |state*
  ;;----------------------------------------------------------------------;;
  (atom {:buffer-info nil :prog-info nil :gl nil :canvas nil}))

(defn- compile-shader
  ;;----------------------------------------------------------------------;;
  [vs fs]
  (info "compile-shader" "Compiling...")

  (when-let [pi (twgl/createProgramInfo
                 (:gl @|state*)
                 #js [vs fs]
                 (fn [msg line]
                   (swap! |state* assoc :prog-err {:msg msg :line line})
                   (.log js/console (str "*** COMPILE ERROR ***\n" msg))))]

    (swap! |state* assoc :prog-info pi :prog-err nil)))

(defn- load-shaders
  ;;----------------------------------------------------------------------;;
  [vs-nom fs-nom]
  (info "load-shaders" "Fetching and compiling: " vs-nom ":" fs-nom)

  (let [vs-file (str "shaders/" vs-nom)
        fs-file (str "shaders/" fs-nom)]
    (go
     (let [msg (<! (util/fetch vs-file))]
       (condp = (:status msg)
         :done (let [msg-fs (<! (util/fetch fs-file))]
                 (condp = (:status msg-fs)
                   :done (compile-shader (:text msg) (:text msg-fs))
                   :error (warn "load-shaders"
                                "Can't load frag shader:" (:desc msg))))

         :error (warn "load-shaders"
                      "Can't load vertex shader:" (:desc msg)))))))

(defn- render
  ;;----------------------------------------------------------------------;;
  [timestamp]
  (let [{:keys [gl canvas prog-info buffer-info]} @|state*
        width (.-clientWidth canvas)
        height (.-clientHeight canvas)]

    (twgl/resizeCanvasToDisplaySize canvas)
    (.viewport gl 0 0 width height)

    (when prog-info
      (.useProgram gl (gobj/get prog-info "program"))
      (twgl/setBuffersAndAttributes gl prog-info buffer-info)
      (twgl/setUniforms prog-info #js {"time" (* timestamp 0.001)
                                       "resolution" #js [width height]})
      (twgl/drawBufferInfo gl buffer-info))

    (js/requestAnimationFrame render)))

(defn- main
  ;;----------------------------------------------------------------------;;
  []
  (info "main" "Starting...")
  (when-let [canvas (dom/getElement "renderCanvas")]
    (when-let [gl (.getContext canvas "webgl")]
      (load-shaders "test.vs" "test.fs")
      (let [bi (twgl/createBufferInfoFromArrays gl #js {"position" +pos+})]
        (swap! |state* assoc :buffer-info bi
               :gl gl :canvas canvas)
        (js/requestAnimationFrame render)))))

(main)

