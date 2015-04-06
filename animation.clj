(ns hard.animation
 (:use arcadia.core hard.core)
  (:import [UnityEngine]))


 ;mechanim animator
(defn ->animator [o]
  (.GetComponentInChildren o UnityEngine.Animator))

(defn play-state [o s] (.Play (->animator o) s))

(defn cross-fade [o s t] (.CrossFade (->animator o) s t))

(defn param-bool 
  ([o s] (.GetBool (->animator o) s))
  ([o s v] (.SetBool (->animator o) s v)))

(defn param-float 
  ([o s] (.GetFloat (->animator o) s))
  ([o s v] (.SetFloat (->animator o) s v)))


;legacy animations

(defn ->animation [o]
  (.GetComponentInChildren o UnityEngine.Animation))

(defcomponent Bone [^float length]
	(Start [this] 
		(! this length 
		  (.magnitude (V- (->v3 (or (first (rest (children (->go this)))) this)) (->v3 this))))
	(OnDrawGizmos [this] 
		(gizmo-color (color 1 0 1))
		(gizmo-point (->v3 this) 0.175)
		(gizmo-line  (->v3 this) (transform-point  (->transform (->go this)) (->v3 0 (.length this) 0) )))
	(OnDrawGizmosSelected [this] 
		(gizmo-color (color 0 1 1))
		(gizmo-point (->v3 this) 0.275)
		(gizmo-line (->v3 this) (transform-point  (->transform (->go this)) (->v3 0 (.length this) 0) ))))

