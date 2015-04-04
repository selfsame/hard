(ns hard.edit
	(:use [hard.core])
	(:import 
		
		     [UnityEngine Gizmos]))

(defn sel [] (Selection/objects))

(defn active [] (Selection/activeGameObject))

(defn clear-flags! [go]
	(import '[UnityEngine HideFlags])
	(set! (.hideFlags go) HideFlags/None))

(defn no-edit! [go]
	(import '[UnityEngine HideFlags])
	(set! (.hideFlags go) HideFlags/NotEditable))

(defn hide! [go]
	(import '[UnityEngine HideFlags])
	(set! (.hideFlags go) HideFlags/HideInHierarchy))

(defn gizmo-color [c]
  (set! Gizmos/color c))
 
(defn gizmo-line [^Vector3 from ^Vector3 to]
  (Gizmos/DrawLine from to))
 
(defn gizmo-ray [^Vector3 from ^Vector3 dir]
  (Gizmos/DrawRay from dir))
 
(defn gizmo-point [^Vector3 v]
  (Gizmos/DrawSphere v 0.075)) 

(defn gizmo-cube [^Vector3 v ^Vector3 s]
  (Gizmos/DrawWireCube v s)) 