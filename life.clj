(ns hard.life
  (:require arcadia.core)
  (:use 
    hard.dispatch
    hard.protocols))

(rule awake [])
(rule start [])
(rule destroy [])
(rule mouse-down [])
(rule mouse-up [])
(rule mouse-enter [])
(rule mouse-over [])
(rule mouse-exit [])
(rule mouse-drag [])
(rule bump-enter [])
(rule bump-exit [])
(rule bump-stay [])
(rule bump-enter-2d [])
(rule bump-exit-2d [])
(rule bump-stay-2d [])
(rule bump-trigger-enter [])
(rule bump-trigger-exit [])
(rule bump-trigger-stay [])
(rule bump-trigger-enter-2d [])
(rule bump-trigger-exit-2d [])
(rule bump-trigger-stay-2d [])


(arcadia.core/defcomponent Handler [^System.String tag ^System.String id]
  (Awake [this] (awake this tag id))
  (Start [this] (use 'hard.dispatch) (start this tag id))
  (OnDestroy [this] (destroy this tag id))
  (OnMouseDown [this] (mouse-down this tag id))
  (OnMouseEnter [this] (mouse-enter this tag id))
  (OnMouseExit [this] (mouse-exit this tag id))
  ;(OnMouseOver [this] (mouseover this tag id))
  (OnMouseUp [this] (mouse-up this tag id))
  (OnMouseDrag [this] (mouse-drag this tag id))

  hard.protocols.IBumpEnter
  (OnBumpEnter [this collider] (bump-enter this collider tag id))
  hard.protocols.IBumpExit
  (OnBumpExit [this collider] (bump-exit this collider tag id))
  ; hard.protocols.IBumpStay
  ; (OnBumpStay [this collider] (bump-stay this collider tag id))
  hard.protocols.IBumpEnter2D
  (OnBumpEnter2D [this collider] (bump-enter-2d this collider tag id))
  hard.protocols.IBumpExit2D
  (OnBumpExit2D [this collider] (bump-exit-2d this collider tag id))
  ; hard.protocols.IBumpStay2D
  ; (OnBumpStay2D [this collider] (bump-stay-2d this collider tag id))
  hard.protocols.IBumpTriggerEnter
  (OnBumpTriggerEnter [this collider] (bump-trigger-enter this collider tag id))
  hard.protocols.IBumpTriggerExit
  (OnBumpTriggerExit [this collider] (bump-trigger-exit this collider tag id))
  ; hard.protocols.IBumpTriggerStay
  ; (OnBumpTriggerStay [this collider] (bump-trigger-stay this collider tag id))
  hard.protocols.IBumpTriggerEnter2D
  (OnBumpTriggerEnter2D [this collider] (bump-trigger-enter-2d this collider tag id))
  hard.protocols.IBumpTriggerExit2D
  (OnBumpTriggerExit2D [this collider] (bump-trigger-exit-2d this collider tag id))
  ; hard.protocols.IBumpTriggerStay2D
  ; (OnBumpTriggerStay2D [this collider] (bump-trigger-stay-2d this collider tag id))
  )
 

(arcadia.core/defcomponent Use [^String ns]
  (Awake [this] 
    (use (symbol ns))
    (awake this ns))
  (Start [this] (start this ns)))

(arcadia.core/defcomponent Update [f]
 (Update [this] ((.f this) this)))

(defn route-update [go f]
  (let [c (.AddComponent go Update)]
    (set! (.f c) f)))