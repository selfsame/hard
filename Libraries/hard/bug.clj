(ns hard.bug
  (:import
    [UnityEngine Color])
  (:use [hard.core]))

(declare make-segment mitosis dna)

(defn  -v [op & more]
	(apply mapv op 
		(map #(cond (number? %) [% % %]
					(vector3? %) [(.x %) (.y %) (.z %)]
					:else %) more)))




(defn parent! [a b]
	(set! (.parent (.transform a)) (.transform b)))

(defn rotation [o]
	(cond (gameobject? o) (.eulerAngles (.rotation (.transform o) ))))

(defn fun-name []
	(let [pre ["an" "in" "on" "un" "og" "aw" "oh" "il" "el"]
		  post ["v" "g" "p" "t" "r" "k" "s" "m"]]
		  (str (rand-nth pre) (rand-nth post) (rand-nth pre))))

(defn make-chunk [k]
	(let [ob (primitive k)
		  c1 (Color. (rand)(rand)(rand)1)
		  c2 (Color. (rand)(rand)(rand)1)]
		(when-let [shader (resource "VERTEX")]
			(set! (.material (.renderer ob)) shader))
		(vertex-colors! ob (yfade c1 c2))

		(local-scale! ob [(+ (rand) 0.5)(+ (rand) 0.5)(+ (rand) 0.5)])
	 ob))
		; (.Rotate (.transform ob) (vec3 (:rotation part)))
		; (position! ob pos)

(defn gen-bug []
	{
	 :scale [(+ (rand) 0.2)
	 		 (+ (rand) 0.5)
	 		 (+ (rand) 0.2)]
	 :symetry (rand-nth [:radial :bilateral])
	:rotation (-v * 20 (-v - [(rand)(rand)(rand)] 0.5))
	 :subdivide (int (* (rand) 12))})



(defn clone-arm [dna base arm]
	(let [a1 (clone arm)]
		(.Rotate (.transform a1) (vec3 [(* (rand) 360) (* (rand) 360)(* (rand) 360)]))
		(parent! a1 base)))

(def dirty (atom nil))

(defn lowkey [] (+ 0.1 (* (rand) 0.4)))

(defn bugstructor [x z data]
	(when-not data (when @dirty (destroy! @dirty) (reset! dirty nil)))
  (let [skyball (find-name "skyball")
  	c1 (Color. (lowkey)(lowkey)(lowkey)1)
		  c2 (Color. (lowkey)(lowkey)(lowkey)1)
  		root (GameObject. (fun-name))
  		loc (mapv * [x 0 z] [8 0 8])
  		dna (or data (dna root))]
  	(vertex-colors! skyball (yfade c1 c2))
  	(reset! dirty root)
  	 (position! root (vec3 loc))
  	 (mitosis dna root root)
  	 (def last-dna dna)
  		 ))


(comment

(doall
	(for [x (range 5)
	      z (range 1)]
	   (bugstructor x z)))


)




(defn gen-limb []
	{:form  (rand-nth [:cube :sphere :cylinder])
	 :scale [(+ (rand) 0.2)
	 		 (+ (rand) 0.2)
	 		 (+ (rand) 0.2)]
	 :rotation (-v * 20 (-v - [(rand)(rand)(rand)] -0.5))
	 :subdivide (int (* (rand) 10))})

(defn make-segment [pdna parent group]
	(let [dna (conj pdna {:subdivide (dec (:subdivide pdna))
	      				  :scale (mapv * (:scale pdna) [0.8 0.9 0.8])})
		  ob (or (clone parent) (primitive (:form dna)))
		  [sx sy sz] (:scale dna)
		  ssy (case (:form dna)
		  	:cylinder (* sy 2.0)
		  	:sphere (* sy 0.8) sy)
		  axial-scale [ssy ssy ssy]]
		
		(local-scale! ob (:scale dna))
		(.Rotate (.transform ob) (vec3 (:rotation dna)))
		(parent! ob group)
		(position! ob (mapv + (unvec (vec3 parent)) (mapv * axial-scale (unvec (.up (.transform parent))))))
		
		(when (pos? (:subdivide dna))
			(make-segment dna ob group))
		group
		))

(defn dna [root]
	(let [chunk-group (GameObject. "chunks")
		ctypes (vec (take 3 (repeatedly #(rand-nth [:cube :sphere :cylinder :capsule])))) ; :sphere :cylinder :capsule
		chunks (mapv #(make-chunk %) ctypes)]
		(mapv #(parent! % chunk-group) chunks)
		(parent! chunk-group root)
	{:idx 0
	:chunks  chunks
	:ctypes ctypes
	;the form progression
	:form (vec (take (+ 6 (rand-int 14)) (repeatedly #(rand-int 3))))
	;base scale multipliers for mitosis
	:sx 0.9
	:sy 0.9
	:sz 0.9
	:rx (* 20  (- (rand) 0.5))
	:ry (* 20  (- (rand) 0.5))
	:rz (* 20  (- (rand) 0.5))
	:R 	(apply merge (take 15 (repeatedly 
		#(identity {[(rand-int 3)(rand-int 3)] 
			(rand-nth [{(rand-nth [:sx :sy :sz]) (+ (rand) 0.4)}
					 {(rand-nth [:rx :ry :rz]) (* (- (rand) 0.5) 120)}
					 {(rand-nth [:rx :rz]) (* (- (rand) 0.5) 120)}
					 {:branch true :rx (* (rand) 70) :rz (* (rand) 70) :sy 0.2}
			]	)})

			)))
	}))

(defn mitosis [dna prev root]

	(let [{:keys [idx chunks ctypes form sx sy sz rx ry rz R]} dna
		special (or (get R [(get form (dec idx))(get form idx)]) {})
		{:keys [sx sy sz rx ry rz]} (merge-with + dna special)

		[psx psy psv] (unvec (local-scale prev))
		newscale (-v * [psx psy psv] [sx sy sz])
		p-axial-offset (or (get 
			{:sphere 0.5
			:cylinder 1
			:capsule 1} (get ctypes (get form (dec idx)))) 0.5)
		axial-offset (or (get 
			{:sphere 0.5
			:cylinder 1
			:capsule 1} (get ctypes (get form idx))) 0.5)
		fpoint (-v + (vec3 prev) (-v * p-axial-offset (.y (local-scale prev)) (.up (.transform prev))))
		newpos (-v + 
				(-v * 
					psy  
					axial-offset 
					(.up (.transform prev)))
				(vec3 prev))
		ob (clone (get chunks (get form idx)))]
		(position! ob fpoint)

		;(position! ob newpos)
		(local-scale! ob newscale)
		 (.Rotate (.transform ob) (vec3 (-v + [rx ry rz] (rotation prev))))
		 (position! ob
		 	(-v + (vec3 ob) (-v * axial-offset (.y (local-scale ob)) (.up (.transform ob)))))
		  ; (position! ob 
		  ; 	(-v + (vec3 ob)	  	
		  ; 		(-v + (-v * (.up (.transform ob)) psy)
		  ;  		(-v * (.up (.transform prev)) -1) )
		  ; 	))

		(parent! ob root)
		(when (< idx (dec (count form)))
			(do
			(mitosis
				(conj dna {:idx (inc idx)})
				ob root)
			(if (get special :branch) 
				(mitosis
				(conj dna {:idx (inc idx) :rx (* rx -1) :rz (* rz -1)})
				ob root)))
		)))

 ;(bugstructor 0 0)

;(.Rotate (.transform ob) (vec3 (mapv + [rx ry rz] (unvec (rotation prev)))))
;(unvec (.up (.transform prev))))

;(vec3 (mapv + [rx ry rz] (unvec (rotation prev))))


(defn easy []
	(bugstructor 0 0 nil))