(ns hard.edit
	(:use [hard.core])
	(:import 
		     [UnityEngine Gizmos]))

(defn sel [] (Selection/objects))

(defn sel! 
	([v] (cond (gameobject? v) (set! (Selection/objects) (into-array [v]))
			   (sequential? v) (set! (Selection/objects) (into-array v))))
	([v & more] (set! (Selection/objects) (into-array (cons v more)))))

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

(defn add-tag [s] (Extras/AddTag (str s)))

(defn scene-use [-ns & more]
	(let [sn (str -ns)
		  go (or (find-name sn) (GameObject. (str "(use " -ns ")")))
		  hook (do (destroy! (.GetComponent go hard.hooks.LifeCycle))
		  		   (.AddComponent go hard.hooks.LifeCycle))
		  {:keys [start update]} (or (first more) {})]
		(! hook ns sn)
		(when start (! hook startfn (str start)))
		(when update (! hook updatefn (str update)))
		(no-edit! go)))