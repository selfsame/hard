 
(ns hard.boiled
	(:use 
		hard.core arcadia.hydrate)
	(:require 
	[clojure.string :as string])
  (:require arcadia.core)
  (:import [UnityEngine]))


 (defn hyphen->camel [charseq]
	(let [parted (partition-by #(= \- %) charseq)]
	(apply str (cons (apply str (first parted))
		(map 
		(fn [col]
			(cond (= '(\-) col) nil
				:else (str (string/upper-case (str (first col)))
						(apply str (rest col)))))
		(rest parted))))))

(defn kw->sym [kw]
	(symbol (hyphen->camel (rest (str kw)))))

(defmacro kset! [value -instance & -more]
	(let [instance (if (vector? -instance) (first -instance) -instance)
		more (if (vector? -instance) (rest -instance) -more)]
		(let [mpath (map kw->sym more)]
		`(set! (.. ~instance ~@mpath) ~value))))

(defmacro kget [-instance & -more]
	(let [instance (if (vector? -instance) (first -instance) -instance)
		more (if (vector? -instance) (rest -instance) -more)]
		(let [mpath (map kw->sym more)]
		`(.. ~instance ~@mpath))))









(defn type-kw [o]
	(cond
		(number? o) :number
		(sequential? o) :sequential
		(vector3? o) :vector3
		(vector2? o) :vector2
		(vector4? o) :vector4
		(color? o) :color
		(transform? o) :transform
		(gameobject? o) :gameobject))


(defmulti from
  (fn [a b] [(type-kw a)(type-kw b)]))

(defmethod from [:gameobject :vector3] 
	[a b] "gob->vec3")

(defmethod from [nil nil] [a b] nil)

(def comp-kw-map
	{:transform UnityEngine.Transform
	 :light UnityEngine.Light
	 :camera UnityEngine.Camera

	 :capsule-collider UnityEngine.CapsuleCollider
	 :box-collider UnityEngine.BoxCollider
	 :sphere-collider UnityEngine.SphereCollider

	 :rigidbody UnityEngine.Rigidbody
	 :hinge-joint UnityEngine.HingeJoint

	 :text-mesh UnityEngine.TextMesh
	 :mesh-renderer UnityEngine.MeshRenderer
	 :skinned-mesh-renderer UnityEngine.SkinnedMeshRenderer

	 :audio-source UnityEngine.AudioSource})

(def prop-kw-map
 {:position #(.position %)
	:rotation #(.rotation %)
	:scale #(.scale %)
	:color #(.color %)
	:background #(.color %)
	:center #(.center %)

	:x #(.x %)
	:y #(.y %)
	:z #(.z %)

	:r #(.r %)
	:g #(.g %)
	:b #(.b %)
	:a #(.a %)

	:intensity #(.intensity %)})

(defn parse [o & more]
	(reduce 
		(fn [res kw]
			(let [compo (get comp-kw-map kw)
						prop (get prop-kw-map kw)
						next (cond compo (try (.GetComponent res compo) 
																(catch Exception e nil))
												prop (try (prop res) 
															(catch Exception e nil)))]
				(or next res)))
		o more))


; (defn pkv [px col]
; 	(mapcat (fn [[k v]] 
; 		(if (map? v)
; 			(pkv (conj px k) v)
; 			[[(conj px k) v]])) col))

; (defn parse-tween [data]
; 	(mapv (fn [[p v]] 
; 		{:target (first p)
; 			:path (vec (rest p))
; 			:value v}) (pkv [] data)))

; (defn hy [ob & more]
; 	(get-in (dehydrate ob) (vec more)))

; (defn hy! [ob map]
; 	(populate! ob map))


; {:box-collider [{:size [2 2 2]}]}
; [:box-collider 0 :size [2 2 2]]
; [:box-collider :size [2 2 2]]

; (defprotocol Positional
;   (pos [x])
;   (from [x y])
;   (to [x z]))

; (defprotocol Directional
;   (up [x])
;   (forward [x])
;   (right [x])
;   (left [x]))

; (defprotocol Countable
;   (-count [a]))

; (defprotocol Lerpable
;   (lerp [a b r]))

; (defprotocol Clampable
;   (clamp [a min max]))

; (defprotocol Invertable
;   (invert [x]))

; (extend-type UnityEngine.Vector3
; 	Positional
;   	(pos [v] v)
;   	(from [v x] (vec3 (-v - v x)))
;   	(to [v x] (vec3 (-v - x v)))
; 	Countable
;  		(-count [v] 3)
; 	Invertable
; 		(invert [v] (vec3 (-v * v -1)))
; 	Lerpable
; 		(lerp [a b r]
; 			(Vector3/Lerp a (vec3 b) r))
; 	Clampable
; 		(clamp [v min max]
; 			(Vector3/Clamp v (vec3 min) (vec3 max))))


; (extend-type UnityEngine.GameObject
; 	Positional
;   	(pos [v] (->v3 v))
;   	(from [v x] (->v3 (-v - (->v3 v) (->v3 x))))
;   	(to [v x] (->v3 (-v - (->v3 x) (->v3 v)))))

; (extend-type UnityEngine.Transform
; 	Positional
;   	(pos [v] (.position v))
;   	(from [v x] (->v3 (-v - (->v3 v) (->v3 x))))
;   	(to [v x] (->v3 (-v - (->v3 x) (->v3 v)))))
