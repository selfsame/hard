
(ns hard.boiled
  (:use hard.core))




(defn- set-v3-part [op o p v]
	(cond 
		(vector3? o) 
		(if op
			(case p
				:x (set! (.x o) (op (.x o) v))
				:y (set! (.y o) (op (.y o) v))
				:z (set! (.z o) (op (.z o) v)))
			(case p
				:x (set! (.x o) v)
				:y (set! (.y o) v)
				:z (set! (.z o) v)))

		(= (type o) UnityEngine.Color) 
		(if op
			(case p
				:r (set! (.r o) (float (op (.r o) v)))
				:b (set! (.b o) (float (op (.b o) v)))
				:g (set! (.g o) (float (op (.g o) v)))
				:a (set! (.a o) (float (op (.a o) v))))
			(case p
				:r (set! (.r o) (float v))
				:b (set! (.b o) (float v))
				:g (set! (.g o) (float v))
				:a (set! (.a o) (float v))))

		(gameobject? o) 
		(let [[x y z] (unvec (vec3 o))
			  mod (get 
			  		(if op {:x [(op v x) y z] :y [x (op v y) z] :z [x y (op v z)]}
			  				{:x [v y z] :y [x v z] :z [x y v]})
			  			 p)]
			  	
			  (position! o mod))))

(defn x! 
	([op o v] (set-v3-part op o :x v))
	([o v] (set-v3-part nil o :x v)))
(defn y! 
	([op o v] (set-v3-part op o :y v))
	([o v] (set-v3-part nil o :y v)))
(defn z! 
	([op o v] (set-v3-part op o :z v))
	([o v] (set-v3-part nil o :z v)))

(defn r! 
	([op o v] (set-v3-part op o :r v))
	([o v] (set-v3-part nil o :r v)))
(defn g! 
	([op o v] (set-v3-part op o :g v))
	([o v] (set-v3-part nil o :g v)))
(defn b! 
	([op o v] (set-v3-part op o :b v))
	([o v] (set-v3-part nil o :b v)))
(defn a! 
	([op o v] (set-v3-part op o :a v))
	([o v] (set-v3-part nil o :a v)))

