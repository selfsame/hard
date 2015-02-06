(ns hard.hooks
	(:use hard.core hard.input arcadia.messages)
	(:require arcadia.core)) 

"CURRENTLY BROKED"
   
(defn have-fun [compo fnstr & more]
	(when (not= "" fnstr)
		(let [bound (resolve (symbol fnstr))]
			(when bound
				(try (apply bound (cons (.gameObject compo) more)) 
	 				(catch Exception e 
	 					(bound)))))))
 
(arcadia.core/defcomponent OnStart [^String ns ^String callfn]
	(Awake [this] (use (symbol ns)))
	(Start [this] (have-fun this callfn)))

(arcadia.core/defcomponent OnUpdate [^String ns ^String updatefn]
	(Awake [this] (use (symbol ns)))
	(Update [this] (have-fun this updatefn)))

(arcadia.core/defcomponent Mouse [ ^String ns ^String down ^String up ^String enter ^String leave ^String over ^Boolean m-over]
	(Awake [this]
		(use 'hard.hooks (symbol ns)))
	(Start [this] (use (symbol ns)))
	(Update [this]
		(let [mouse-hits (ray-hits (mouse-ray) 5000)
			  overme (first (filter 
					#(= (parent-component (.collider %) (type this)) this) (vec mouse-hits)))]
			(if overme
				(do (have-fun this over)
					(when (false? (.m-over this))
						(do (have-fun this enter)
							(set! (.m-over this) true)))
					(when (mouse-down?)
						(have-fun this down))
					(when (mouse-up?)
						(have-fun this up)))
				(when (true? (.m-over this))
						(do (have-fun this leave)
							(set! (.m-over this) false)))))))  


(arcadia.core/defcomponent MouseDown [^String ns ^String mousedown]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(OnMouseDown [this] (have-fun this mousedown))) 

(arcadia.core/defcomponent MouseEnter [^String ns ^String mouseenter]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(OnMouseEnter [this] (have-fun this mouseenter))) 

(arcadia.core/defcomponent MouseExit [^String ns ^String mouseexit]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(OnMouseExit [this] (have-fun this mouseexit))) 

(arcadia.core/defcomponent MouseOver [^String ns ^String mouseover]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(OnMouseOver [this] (have-fun this mouseover))) 

(arcadia.core/defcomponent MouseUp [^String ns ^String mouseup]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(OnMouseUp [this] (have-fun this mouseup))) 

(arcadia.core/defcomponent MouseDrag [^String ns ^String mousedrag ^Vector2 previous]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(OnMouseDrag [this] 
		(let [m (mouse-pos)
			  p [(.x previous) (.y previous)]]
			(when-not (= m p)
				(set! (.previous this) (Vector2. (first m) (last m)))
				(have-fun this mousedrag (mapv - m p)))))) 

(arcadia.core/defcomponent LifeCycle [ ^String ns ^String start ^String update]
	(Awake [this]
		(use 'hard.hooks (symbol ns)))
	(Start [this] (use (symbol ns))
		(have-fun this start))
	(Update [this] (have-fun this update))) 


(arcadia.core/defcomponent Trigger [ ^String ns ^String enter ^String exit ^String stay]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(OnTriggerEnter [this collider] (have-fun this enter collider))
	(OnTriggerStay [this collider] (have-fun this stay collider))
	(OnTriggerExit [this collider] (have-fun this exit collider))) 

(arcadia.core/defcomponent Collision [ ^String ns ^String enter ^String exit ^String stay]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(OnCollisionEnter [this collision] (have-fun this enter collision))
	(OnCollisionStay [this collision] (have-fun this stay collision))
	(OnCollisionExit [this collision] (have-fun this exit collision))) 
  

  (log "hard.hooks on")