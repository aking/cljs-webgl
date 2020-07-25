(ns webgl.util
  (:require
   [clojure.core.async :refer [chan put! go go-loop]]
   [clojure.repl :refer-macros [doc find-doc source]]))

;;;======================================================================;;;
;; Javascript...
;;
;;------------------------------------------------------------------------;;
(defn js-eval [js-form]
  (js/eval js-form))

(defn sleep
  ;;----------------------------------------------------------------------;;
  [ms f]
  (js/setTimeout f ms))

;;------------------------------------------------------------------------;;
(defn debugger []
  #_(js* "debugger"))

;;;======================================================================;;;
;; Loggers
;;
(def +loggers+ (atom '()))
(def +types+ {:debug "DBG" :warn "WRN" :info "INF" :error "ERR"})
(def +type-styles+ {:none "color: white; font-weight: bold;"
                    :debug "color: blue; font-weight: bold; font-size: 100%;"
                    :warn "color: orange; font-weight: bold; font-size: 90%;"
                    :info "color: green; font-weight: normal; font-size: 85%"
                    :error "color: red; font-weight: bold;"})


;;------------------------------------------------------------------------;;
(defn reset-loggers []
  (reset! +loggers+ '()))

;;------------------------------------------------------------------------;;
(defn register-logger [cb]
  (swap! +loggers+ conj cb))

;;------------------------------------------------------------------------;;
(defn ->console [lvl msg]
  (.log js/console msg (lvl +type-styles+) (:none +type-styles+))
  ; (println msg)
  (doall (map #(% msg) @+loggers+)))

;;------------------------------------------------------------------------;;
(defn make-logger
  [log-type ns-name]
  (fn [fn-name & msg]
    (let [msg* (if msg (apply str msg) fn-name)
          fn-name* (if msg fn-name "")]
      (->console log-type (str "%c" (log-type +types+)
                               "[" ns-name ":" fn-name* "]%c "
                               msg*))
      nil)))

;;;======================================================================;;;
;; Conversions
;;
;;
;;------------------------------------------------------------------------;;
(defn str->number [n]
  (let [number (js/parseFloat n)]
    (if (and (number? number)
             (not (js/isNaN number)))
      number)))

;;------------------------------------------------------------------------;;
(defn str->integer [n]
  (let [number (js/parseInt n)]
    (if (and (number? number)
             (not (js/isNaN number)))
      number)))

(defn stringify [v]
  (.stringify js/JSON (if (object? v)
                        v
                        (clj->js v))))

;;;======================================================================;;;
;; Math
;;
;;
;;------------------------------------------------------------------------;;
(defn wrap-max [x max]
  (mod (+ max (mod x max)) max))

(defn wrap [x min max]
  (+ min (wrap-max (- x min) (- max min))))

;;;======================================================================;;;
;; Web Helpers
;;
;;
;;------------------------------------------------------------------------;;
(defn fetch
  "Retrieves a remote file from server:

  (go
    (let [fname 'afile.txt'
          msg (async/<! (fetch fname))]
      (condp = (:status msg)
          :done (info 'FILE:' (:text msg))
          :error (warn (str 'ERR: [' (:code msg) ' ' (:desc msg)))"

  [url]
  (let [c (chan 1)
        &fetch (.fetch js/window url)]
    (let [&text (.then &fetch
                       (fn [response]
                         (if (.-ok response)
                           (.text response)
                           (put! c {:status :error
                                    :code (.-status response)
                                    :desc (.-statusText response)}))))]
      (.then &text
             (fn [text]
               (put! c {:status :done :text text}))))
    c))

