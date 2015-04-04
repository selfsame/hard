(ns hard.hooks
	(:use hard.core hard.input arcadia.messages)
	(:require arcadia.core)
	(:import [UnityEngine])) 
   
(defn have-fun [compo fnstr & more]
	(when (not= "" fnstr)
		(let [bound (resolve (symbol fnstr))]
			(when bound
				(apply bound (cons (.gameObject compo) more))))))


(arcadia.core/defcomponent Mouse [ ^String ns ^String down ^String up ^String enter ^String exit ^String over ^Boolean m-over]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(Start [this] (use 'hard.hooks (symbol ns)))
	(OnMouseDown [this] (have-fun this down))
    (OnMouseUp [this] (have-fun this up))
	(OnMouseEnter [this] (have-fun this enter))
	(OnMouseExit [this] (have-fun this exit))
	(OnMouseOver [this] (have-fun this over)))  
 

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
			  p [(.x previous) (.y previous)]
			  delta (mapv - m p)]
			(when-not (= m p)
				(set! (.previous this) (Vector2. (first m) (last m)))
				(when (= [true true] (mapv #(< % 10) delta))
				 (have-fun this mousedrag (mapv - m p)))))))

(arcadia.core/defcomponent OnStart [^String ns ^String start]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(Start [this] (have-fun this start)))

(arcadia.core/defcomponent OnUpdate [^String ns ^String update]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(Update [this] (have-fun this update)))

(arcadia.core/defcomponent DestroyHook [^String ns ^String destroyfn]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(OnDestroy [this] (have-fun this destroyfn)))

(arcadia.core/defcomponent LifeCycle [ ^String ns ^String startfn ^String updatefn ^String destroyfn]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(Start [this] (have-fun this startfn))
	(Update [this] (have-fun this updatefn))
	(OnDestroy [this] (have-fun this destroyfn))) 


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

(arcadia.core/defcomponent Gui [^String ns ^String gui-fn]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(OnGUI [this] (have-fun this gui-fn))) 

  (log "hard.hooks on")  