(ns cage-match.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [chan <! >! put! close! alts! timeout sliding-buffer]]
            [cljs.core.async.impl.protocols :as pa]
            [cljs.reader :as reader]
            [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui ui]]
            [om.dom :as dom]
            [cljs-audiocapture.core :refer [capture-audio pcm-frames->wav]]
            cljsjs.filesaverjs))

(enable-console-print!)

(def app-state (atom nil))

(defonce file-temp (atom nil))

(defn init-processing-fn
  [begin>]
  (fn
    [e]
    (.stopPropagation e)
    (.preventDefault e)
    (let [file (-> e .-dataTransfer .-files (.item 0))]
      (reset! file-temp file))
    (go
      (>! begin> :begin))))

(defn uploader-ui
  [begin>]
  (ui Object
      (render [this]
              (let [{:keys [count]} (om/props this)]
                (dom/div #js {:className "box red"
                              :onDragEnter (fn [e]
                                             (.stopPropagation e)
                                             (.preventDefault e))
                              :onDragOver (fn [e]
                                            (.stopPropagation e)
                                            (.preventDefault e))
                              :onDrop (init-processing-fn begin>)} nil)))))
(def reconciler
  (om/reconciler {:state app-state}))

(defn recording-loop
  [begin< end< audio-chan publish>]
  (go-loop []
    (<! begin<)
    (println "Starting recording")
    (put! audio-chan :start)
    (<! (timeout 5000))
    (println "Ending recording")
    (put! audio-chan :pause)
    ;; make sure all frames are collected
    (<! (timeout 1000))
    (println "Publishing")
    (>! publish> :publish)
    (println "Done publishing")
    (<! end<)
    (recur)))

(defn init-recording
  [begin<]
  (go
    (let [{:keys [audio-chan error]} (<! (capture-audio (sliding-buffer 10)))
          publish< (chan)
          end< (chan)]
      (if error
        (js/console.error error)
        (do
          (recording-loop begin< end< audio-chan publish<)
          (go-loop [audio-data []]
            (let [[v c] (alts! [audio-chan publish<])]
              (if-not (= c publish<)
                (recur (conj audio-data v))
                (do (js/saveAs (pcm-frames->wav audio-data) "mashup.wav")
                    (>! end< :end)
                    (recur []))))))))))

(defn init
  []
  (let [begin (chan)]
    (init-recording begin)
    (om/add-root! reconciler
                  (uploader-ui begin) (gdom/getElement "app"))))

(init)
