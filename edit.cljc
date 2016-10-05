(ns hard.edit
	(:use [hard.core])
	(:import [UnityEngine]))

'(defn active [] (Selection/activeGameObject))

'(defn sel [] (Selection/objects))

'(defn sel! 
 	([v] (cond (gameobject? v) (set! (Selection/objects) (into-array [v]))
 			   (sequential? v) (set! (Selection/objects) (into-array v))))
 	([v & more] (set! (Selection/objects) (into-array (cons v more)))))

(defn clear-flags! [go]
	(import '[UnityEngine HideFlags])
	(set! (.hideFlags go) HideFlags/None))

(defn not-editable! [go]
	(import '[UnityEngine HideFlags])
	(set! (.hideFlags go) HideFlags/NotEditable))

(defn editable! [go]
  (import '[UnityEngine HideFlags])
  (set! (.hideFlags go) HideFlags/None))

(defn hide! [go]
	(import '[UnityEngine HideFlags])
	(set! (.hideFlags go) HideFlags/HideInHierarchy))
