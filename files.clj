(ns webgl.files)

;;-----------------------------------;;----------------------------------;;
(def +webgl-files+

  {:--webgl--
   [;;_______________________________;;
    ;; Common client code logic
    "src/webgl/core.cljs"
    "src/webgl/util.cljs"]

   :--glsl--
   [;;_______________________________;;
    "resources/public/shaders/test.fs"
    "resources/public/shaders/test.vs"]

   :--html--
   [;___________________________
    "resources/public/dev.html"
    "resources/public/index.html"
    "resources/public/css/style.css"]


   ;; ---- PROJECT ----
   :--externs--
   [;___________________________
    "resources/externs/twgl.extern.js"]

   :--project--
   [;___________________________
    "files.clj"
    "deps.edn"
    "build-adv.cljs.edn"
    "build-dev.cljs.edn"
    "figwheel-main.edn"
    "README.md"]})

