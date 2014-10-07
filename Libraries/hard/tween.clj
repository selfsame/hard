(ns hard.tween
	(:use hard.core hard.input hard.boiled)
	(:require unity.core 
	[clojure.string :as string])
	(:import [UnityEngine Color Vector3]))
 
(declare update)

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

(defmacro kget! [-instance & -more]
	(let [instance (if (vector? -instance) (first -instance) -instance)
		more (if (vector? -instance) (rest -instance) -more)]
		(let [mpath (map kw->sym more)]
		`(.. ~instance ~@mpath))))



(unity.core/defcomponent Tween []
	(Awake [this]
		(use 'hard.tween))
	(Update [this]
		(update (UnityEngine.Time/deltaTime))))

(defn- wake! []
	(when-not (find-name "__tweener__")
		(let [c (GameObject. "__tweener__")]
			(.AddComponent c "Tween"))))

(def ^:private UID (atom 0))

(defn- get-uid [] (swap! UID inc))

(def ^:private tweens (atom {}))

(def ^:private handlers (atom 
	{[:position] {:get #(vec3 %1)
				  :set #(position! %1 %2)}
	[:local-scale] {:get #(vec3 (local-scale %1))
				  :set #(local-scale! %1 %2)}
	[:euler] {:get #(rotation %1)
			 :set #(rotation! %1 %2)}
	[:x] {:get #(.x (vec3 %1))
			 :set #(x! %1 %2)}
	[:y] {:get #(.y (vec3 %1))
			 :set #(y! %1 %2)}
	[:z] {:get #(.z (vec3 %1))
			 :set #(z! %1 %2)}
				  }))

(defn register [path getter setter]
	(swap! handlers #(conj % {path {:get getter :set setter}})))

(defn- pull [gob path]
	(when-let [f (get @handlers path)]
			((:get f) gob)))

(defn- push [gob path v]
	(when-let [f (get @handlers path)]
			((:set f) gob v)))

(defn cap-ratio [n]
	(if (> n 1.0) 1.0 n))

(defn- interpolate [sv v r]
	(cond 
		(number? sv)
		(float (+ sv (* (- v sv) r)))
		(= (type sv) UnityEngine.Color)
		(Color/Lerp sv v r)
		(= (type sv) UnityEngine.Vector3)
		(Vector3/Lerp sv v r)
		:else
		(-v + sv (-v * (-v - v sv) r))))

(defn- tweak [[uid tw] now]
	(let [target (:target tw)
		  gob (first target)
		  path (rest target)
		  start (:start tw)
		  duration (:duration tw)
		  delta (- now start)
		  ratio (cap-ratio (/ (- duration (- duration delta)) duration))
		  res (interpolate (:start-value tw) (:value tw) ratio)]
	(push gob path res)
	(if (> now (:end tw))
		(do (when (fn? (:callback tw))
				((:callback tw) (first target)))
			true) 
		false)))
  
(defn update [delta]
	(let [time (UnityEngine.Time/time)]
		(let [to-remove (into {} 
							(filter 
								#(try (tweak % time) 
									(catch Exception e (do 
										(log (str e)) true))) @tweens))
			 rem-keys (keys to-remove)]
		(swap! tweens #(apply dissoc % rem-keys)))))

(defn- cast-as [v t]
	(if (= t (type v)) v
		(case t
			System.Int64 (int v)
			System.Double (float v)
			UnityEngine.Vector3 (vec3 v)
			UnityEngine.Color (color v)
			(if (sequential? v) (vec3 v) v))))

(defn add [target -value & more]
	(wake!)
	;(log [(first target) (vec (rest target))])
	(when-let [start-value (pull (first target) (vec (rest target)))]
		(let [val-type (type start-value)
			  uid (get-uid)
			  start (UnityEngine.Time/time)
			  callback (first (filter fn? more))
			  duration  (or (first (filter number? more)) 1.0)
			  flags (set (filter keyword? more))
			  value (if (flags :relative)
			  			(cond (number? -value) (+ -value start-value)
			  				:else (-v + -value start-value))
			  			-value)
			  opts (set more)]
			(swap! tweens 
				#(conj % 
					{uid {:value (cast-as value val-type)
						  :start-value start-value
						  :start start
						  :duration duration
						  :end (+ start duration)
						  :target target
						  :callback callback
						  :flags flags}}))		  
			uid)))

(defn delete [uid]
	(swap! tweens #(dissoc % uid)))

(register [:camera :field-of-view] 
	#(.fieldOfView (component % "Camera")) 
	#(set! (.fieldOfView (component %1 "Camera")) %2))

(register [:color] 
	#(.color % ) 
	#(set! (.color %1) %2))

(register [:text-mesh :color] 
	#(.color (component % "TextMesh")) 
	#(set! (.color (component %1 "TextMesh")) %2))

(register [:blend-shape 0] 
	#(.GetBlendShapeWeight (component % "SkinnedMeshRenderer") 0)
	#(.SetBlendShapeWeight (component %1 "SkinnedMeshRenderer") 0 %2))

(register [:blend-shape 1] 
	#(.GetBlendShapeWeight (component % "SkinnedMeshRenderer") 1)
	#(.SetBlendShapeWeight (component %1 "SkinnedMeshRenderer") 1 %2))

(register [:blend-shape 2] 
	#(.GetBlendShapeWeight (component % "SkinnedMeshRenderer") 2)
	#(.SetBlendShapeWeight (component %1 "SkinnedMeshRenderer") 2 %2))
 

