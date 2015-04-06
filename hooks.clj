(ns hard.hooks
	(:use hard.core hard.input hard.protocols arcadia.messages)
	(:require arcadia.core)
	(:import [UnityEngine])) 
   
(defn have-fun [compo fnstr & more]
	(try
		(when (not= "" fnstr)
			(when-let [bound (resolve (symbol fnstr))]
				(apply bound (cons (.gameObject compo) more))))
		(catch Exception e (log e))))


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

(arcadia.core/defcomponent LifeCycle [ ^String ns ^String startfn ^String updatefn ^String destroyfn]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(Start [this] (have-fun this startfn))
	(Update [this] (have-fun this updatefn))
	(OnDestroy [this] (have-fun this destroyfn))) 


(arcadia.core/defcomponent Collision [ ^String ns ^String enter ^String exit ^String stay]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(Start [this] (use 'hard.hooks (symbol ns)))
	hard.protocols.IBumpEnter
	(OnBumpEnter [this collider] (have-fun this enter collider))  
	hard.protocols.IBumpExit
	(OnBumpExit [this collider] (have-fun this exit collider))  
	hard.protocols.IBumpStay
	(OnBumpStay [this collider] (have-fun this stay collider)))

(arcadia.core/defcomponent Collision2D [ ^String ns ^String enter ^String exit ^String stay]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(Start [this] (use 'hard.hooks (symbol ns)))
	hard.protocols.IBumpEnter2D
	(OnBumpEnter2D [this collider] (log "ENTER") (have-fun this enter collider))  
	hard.protocols.IBumpExit2D
	(OnBumpExit2D [this collider] (have-fun this exit collider))  
	hard.protocols.IBumpStay2D
	(OnBumpStay2D [this collider] (have-fun this stay collider)))

(arcadia.core/defcomponent Trigger [ ^String ns ^String enter ^String exit ^String stay]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(Start [this] (use 'hard.hooks (symbol ns)))
	hard.protocols.IBumpTriggerEnter
	(OnBumpTriggerEnter [this collider] (have-fun this enter collider))  
	hard.protocols.IBumpTriggerExit
	(OnBumpTriggerExit [this collider] (have-fun this exit collider))  
	hard.protocols.IBumpTriggerStay
	(OnBumpTriggerStay [this collider] (have-fun this stay collider)))

(arcadia.core/defcomponent Trigger2D [ ^String ns ^String enter ^String exit ^String stay]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(Start [this] (use 'hard.hooks (symbol ns)))
	hard.protocols.IBumpTriggerEnter2D
	(OnBumpTriggerEnter2D [this collider] (log "ENTER") (have-fun this enter collider))  
	hard.protocols.IBumpTriggerExit2D
	(OnBumpTriggerExit2D [this collider] (have-fun this exit collider))  
	hard.protocols.IBumpTriggerStay2D
	(OnBumpTriggerStay2D [this collider] (have-fun this stay collider)))

(arcadia.core/defcomponent Gui [^String ns ^String gui-fn]
	(Awake [this] (use 'hard.hooks (symbol ns)))
	(OnGUI [this] (have-fun this gui-fn))) 

  (log "hard.hooks on")  