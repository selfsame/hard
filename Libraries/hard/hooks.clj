(ns hard.hooks
	(:use hard.core hard.input unity.messages)
	(:require unity.core)) 
  
(defn have-fun [compo fnstr & more]
	(when (not= "" fnstr)
		(let [bound (resolve (symbol fnstr))]
			(when bound
				(try (apply bound (cons (.gameObject compo) more)) 
	 				(catch Exception e (log (str e))))))))

(unity.core/defcomponent Mouse [ ^String ns ^String down ^String up ^String enter ^String leave ^String over ^Boolean m-over]
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

(unity.core/defcomponent LifeCycle [ ^String ns ^String start ^String update]
	(Awake [this]
		(use 'hard.hooks (symbol ns)))
	(Start [this] (use (symbol ns))
		(have-fun this start))
	(Update [this] (have-fun this update))) 


(unity.core/defcomponent Trigger [ ^String ns ^String enter ^String exit ^String stay]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(OnTriggerEnter [this collider] (have-fun this enter collider))
	(OnTriggerStay [this collider] (have-fun this stay collider))
	(OnTriggerExit [this collider] (have-fun this exit collider))) 

(unity.core/defcomponent Collision [ ^String ns ^String enter ^String exit ^String stay]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(OnCollisionEnter [this collision] (have-fun this enter collision))
	(OnCollisionStay [this collision] (have-fun this stay collision))
	(OnCollisionExit [this collision] (have-fun this exit collision))) 
