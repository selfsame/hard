
(ns hard.core
  (:require arcadia.core clojure.string)
  (:import
    [UnityEngine Debug Resources GameObject PrimitiveType Application Color Input Screen Gizmos]))
 
(declare position!)

(defn log 
  ([x] (UnityEngine.Debug/Log x))
  ([x & more] (UnityEngine.Debug/Log (apply str (cons x more)))))

(defn vector2? [x] (instance? UnityEngine.Vector2 x))
(defn vector3? [x] (instance? UnityEngine.Vector3 x))
(defn vector4? [x] (instance? UnityEngine.Vector4 x))
(defn transform? [x] (instance? UnityEngine.Transform x))
(defn quaternion? [x] (instance? UnityEngine.Quaternion x))
(defn color? [x] (instance? UnityEngine.Color x))
(defn gameobject? [x] (instance? UnityEngine.GameObject x))
(defn component? [x] (instance? UnityEngine.Component x))

(defn- get-or [col idx nf] (or (get col idx) nf))
    
(defn- -count [o]
  (cond (number? o) 1
        (sequential? o) (count o)
        (vector3? o) 3
        (vector2? o) 2
        (vector4? o) 4
        (color? o) 4)) 

(defn- -vec [o]
  (cond (number? o) [o]
    (vector2? o) [(.x o)(.y o)]
    (vector3? o) [(.x o)(.y o)(.z o)]
    (vector4? o) [(.x o)(.y o)(.z o)(.w o)]
    (color? o) [(.r o)(.g o)(.b o)(.a o)] 
  :else
  (try (vec o) (catch Exception e (str e)))))

(defn- operate [op -a -b]
  (let [c (max (-count -a)(-count -b))
        a (if (number? -a) (vec (take c (repeat -a)))
              (-vec -a))
        b (if (number? -b) (vec (take c (repeat -b)))
              (-vec -b))]
    (map #(op (get-or a % 0) (get-or b % 0)) (range c))))

(defn- reduce-operate [op col]
  (vec (reduce #(operate op %1 %2) col)))

(defn v+ [& more] (reduce-operate + more))
(defn v- [& more] (reduce-operate - more))
(defn v* [& more] (reduce-operate * more))
(defn vdiv [& more] (reduce-operate / more))
(defn -v [op & more] (reduce-operate op more)) 

(defn V+ [^Vector3 a ^Vector3 b] (Vector3/op_Addition a b))
(defn V- [^Vector3 a ^Vector3 b] (Vector3/op_Subtraction a b))
(defn V* [a b] (Vector3/op_Multiply a b))
(defn V÷ [a b] (Vector3/op_Division a b))
(defn Vx [^Vector3 a ^Vector3 b] (Vector3/Cross a b))

(defn find-name [str] (. GameObject (Find str)))

(defn ->go [v]
  (cond (gameobject? v) v   
      (sequential? v) nil
      (string? v) (try (find-name v) (catch Exception e nil))
      :else (try (.gameObject v) (catch Exception e nil))))

(defn ->transform [v]
  (cond (transform? v) v
      :else (if-let [o (->go v)] (.transform o) nil)))

(defn ->v3 
  ([] (Vector3. 0 0 0))
  ([a b] (Vector3. a b 0))
  ([a b c] (Vector3. a b c))
  ([o] 
  (cond 
    (vector3? o) o
    (gameobject? o) (.position (.transform o))
    (number? o) (Vector3. o o o)
    (sequential? o) (Vector3. (get-or o 0 0) (get-or o 1 0) (get-or o 2 0))
    
    (vector2? o) (Vector3. (.x o) (.y o) 0)
    (vector4? o) (Vector3. (.x o) (.y o) (.z o))
    (quaternion? o) (Vector3. (.x o)(.y o)(.z o))
    (color? o) (Vector3. (.r o)(.g o)(.b o))
    (transform? o) (.position o)
    :else
    (try (.position (.transform (.gameObject o)))
        (catch Exception e (type o))))))

(defn ->vec [o]
  (cond 
    (vector3? o) [(.x o)(.y o)(.z o)]
    (vector2? o) [(.x o)(.y o)]
    (vector4? o) [(.x o)(.y o)(.z o)(.w o)]
    (quaternion? o) [(.x o)(.y o)(.z o)(.w o)]
    (color? o) [(.r o)(.g o)(.b o)(.a o)]
    :else (vec o)))



(defn -editor? []
  (. Application isEditor)) 

(defn playing? []
  (. Application isPlaying)) 

(defn load-scene [sn]
  (log (str "loading scene " sn "..."))
  (Application/LoadLevel sn))

(defn quit []
  (Application/Quit))

(defn screen-size []
  [(Screen/width)(Screen/height)])

(defn main-camera [] (UnityEngine.Camera/main))

(defn resource [s] (UnityEngine.Resources/Load s))

(defn null? [gob]
  (and (gameobject? gob) (Extras/nullObject gob)))

(defn destroy! [o]
  (if (sequential? o)
    (mapv destroy! o)
    (let [o (if (component? o) o (->go o))]
      (if (-editor?)
        (. GameObject (DestroyImmediate o))
        (. GameObject (Destroy o))))))

(def primitive! arcadia.core/create-primitive)

(defonce CLONED (atom []))

(defn clone!
  ([ref] (clone! ref nil))
  ([ref pos]
    (let [source (cond (string? ref) (find-name ref)
                       (keyword? ref) (resource (clojure.string/replace (subs (str ref) 1) #"[:]" "/")) 
                       :else ref)
        pos  (if pos (->v3 pos) (->v3 source))
        quat  (.rotation (.transform source))
        gob (. GameObject (Instantiate source pos quat))]
      (set! (.name gob) (.name source))
      (swap! CLONED #(cons gob %))
      gob)))

(defn clear-cloned! [] 
  (mapv destroy! @CLONED) 
  (reset! CLONED []))


 
;uh.. so this is not really saving much typing
(defn color 
  ([col] (if (> (count col) 2) (apply color (take 4 col)) (color 0 0 0 0)))
  ([r g b] (color r g b 1.0))
  ([r g b a] (Color. r g b a)))
 


(defn- clamp-v3 [v3 min max]
  (let [v (->vec (->v3 v3))
      res (mapv #(Mathf/Clamp % min max) v)]
      (->v3 res)))


;The following functions assume gameobject args, I'm big on that but i guess they should also
;accept transforms
 
(defn parent! [a b]
  (set! (.parent (.transform a)) (.transform b)))

(defn unparent! ^GameObject [^GameObject child]
  (set! (.parent (.transform child)) nil) child)

(defn world-position [o]
  (when-let [o (->go o)] (.TransformPoint (.transform o) (->v3 o))))

(defn position! [o pos]
  (set! (.position (.transform o)) (->v3 pos)))

(defn local-direction [o v]
  (when-let [o (->go o)]
    (let [[x y z] (->vec v)]
      (.TransformDirection (.transform o) x y z))))

(defn transform-point [o v]
  (when-let [o (->go o)]
    (.TransformPoint (.transform o) (->v3 v))))

(defn inverse-transform-point [o v]
  (when-let [o (->go o)]
    (.InverseTransformPoint (.transform o) (->v3 v))))

(defn inverse-transform-direction [o v]
  (when-let [o (->go o)]
    (.InverseTransformDirection (.transform o) (->v3 v))))

(defn move-towards [v1 v2 step]
  (Vector3/MoveTowards v1 v2 step))

(defn lerp [v1 v2 ratio]
  (Vector3/Lerp (->v3 v1) (->v3 v2) ratio))

(defn local-scale [o]
  (when-let [o (->go o)] (.localScale (.transform o) )))

(defn local-scale! [o v]
  (when-let [o (->go o)] (set! (.localScale (.transform o)) (->v3 v))))

(defn rotate-around! [o point axis angle]
  (when-let [o (->go o)]
  (. (.transform o) (RotateAround (->v3 point) (->v3 axis) angle))))

(defn rotation [o]
  (when-let [o (->go o)] (.eulerAngles (.rotation (.transform o) ))))

(defn rotate! [o rot]
  (when-let [o (->go o)]
    (.Rotate (.transform o) (->v3 rot))))

(defn rotation! [o rot]
  (when-let [o (->go o)]
    (set! (.eulerAngles (.transform o)) (clamp-v3 rot 0 360))))

(defn look-at! 
  ([a b] (.LookAt (->transform a) (->v3 b)))
  ([a b c] (.LookAt (->transform a) (->v3 b) (->v3 b))))

(defn look-quat [a b]
  (Quaternion/LookRotation  (->v3 (v- (->v3 b) (->v3 a)))))

(defn lerp-look! [a b ^double v]
  (let [at (->transform a)
        aq (.rotation at)
        lq (look-quat a b)]
    (set! (.rotation at) (Quaternion/Lerp aq lq (float v)))))

 
 
 
(defn ^:private appropiate-game-object [reference]
  (cond (nil? reference) nil
    (gameobject? reference) reference
    :else (try (.gameObject reference) (catch Exception e nil))))

(defn component [o sym]
  (when-let [o (->go o)]
    (.GetComponent o sym)))

(defn parent-component [thing sym]
  (when-not (string? sym)
    (when-let [gob (->go thing)]
      (.GetComponentInParent gob sym))))

(defn child-component [thing sym]
  (when-not (string? sym)
    (when-let [gob (->go thing)]
      (.GetComponentInChildren gob sym))))
  
(defn components [thing sym]
  (when-let [gob (->go thing)]
    (.GetComponents gob sym)))

(defn parent-components [thing sym]
  (when-not (string? sym)
    (when-let [gob (->go thing)]
      (.GetComponentsInParent gob sym))))

(defn child-components [thing sym]
  (when-not (string? sym)
    (when-let [gob (->go thing)]
      (.GetComponentsInChildren gob sym))))

(defn sub-forms [go]
  (rest (child-components go UnityEngine.Transform)))
 
(defn children [go]
  (filter #(= (->transform go) (.parent %)) (sub-forms go)))

(defn top-forms [go]
  (butlast
    (loop [o (->transform go) col '()]
    (if-not (.parent o) (cons o col)
      (recur (.parent o) (cons o col))))))


(defn child-named 
  ([go s & more]
    (map #(child-named go %) (cons s more)))
  ([go s]
    (let [ts (child-components go UnityEngine.Transform)]
      (first (take 1 (filter #(= (.name %) s) (map ->go ts)))))))



(defn rand-vec [& more]
  (mapv (fn [col]
    (cond (number? col) (rand col)
    (sequential? col) 
    (case (count col) 
      0 (rand) 
      1 (rand (first col))
      2 (+ (rand (apply - (reverse col))) (first col))
      (rand)
    :else (rand)))) more))

;MACROS

(defmacro each [col bindings & code]
  `(mapv (fn ~bindings ~@code) ~col))

(defmacro mapfn [bindings & more]
  (let [code (butlast more)
      col (last more)]
    `(map (fn ~bindings ~@code) ~col)))

(defmacro when= [a b & body]
    `(~'when (~'= ~a ~b) ~@body))

(defmacro if= [a b & body]
    `(if (= ~a ~b) ~@body))

(defmacro ? [& body]
  (let [[conds _ elses] (partition-by #(not= :else %) body)]
    (if elses
      `(~'cond ~@conds :else (~'do ~@elses))
      `(~'cond ~@conds))))

(defmacro ! [& body]
  (let [value (last body)
        access (butlast body)]
    `(set! (.. ~@access) ~value)))



;Infix macro 
;"PEMDAS is common. It stands for Parentheses, Exponents, Multiplication, Division, Addition, Subtraction"
(def ^:private ordered-ops ['* 'v* 'V* '/ 'vdiv 'V÷ '+ 'v+ 'V+ '- 'v- 'V-])

(defn ^:private non-op? [x] (if ((set ordered-ops) x) false true))

(defn ^:private num-or-seq? [e] (or (number? e) (sequential? e)))

(defn ^:private group [col op] 
  (let [pass1 (partition-by #(or (non-op? %) (= op %)) col)]
    (map 
      #(if-not (> (count %) 1)
        (first %)
        (let [de-opped (filter non-op? %)]
          (if (not= (count de-opped) (count %))
            (cons op (filter non-op? %))
            %))) 
      pass1)))

(defn ^:private red-inf [col]
  (let [col-2 (map #(if (and (list? %) (not= 1 (count %))) (red-inf %) %) col)]
   (first (reduce group col-2 ordered-ops))))

(defmacro $ [& more]
  (let [transformed (red-inf more)]
  `(~@transformed)))

(defn ->comp [o c] (.GetComponent o c))
(defn has? [o c] (if (->comp o c) true false))

(defn- un-dot [syms]
  (loop [col syms] 
    (if (> (count col) 2)
      (let [[obj op field] (take 3 col)
            form (cond (= '. op) (list op obj field)
                       (= '> op) (list '.GetComponent obj (str field)))]
        (recur (cons form (drop 3 col))))
        (first col))))

(defmacro .* [& more]
  (let [address (last more)
        matcher (re-matcher #"[\>\.]|[^\>\.]+" (str address))
        broken (loop [col []] (if-let [res (re-find matcher)] (recur (conj col res)) col))
        s-exp (un-dot (concat (butlast more) (map symbol broken)))]
  `(~@s-exp)))




(defn material [o] (.material (.GetComponent (->go o) UnityEngine.Renderer)))
(defn text-mesh [o] (.text (.GetComponent (->go o) "TextMesh")))




(defn gizmo-color [c]
  (set! Gizmos/color c))
 
(defn gizmo-line [^Vector3 from ^Vector3 to]
  (Gizmos/DrawLine from to))
 
(defn gizmo-ray [^Vector3 from ^Vector3 dir]
  (Gizmos/DrawRay from dir))
 
(defn gizmo-point 
  ([^Vector3 v]
    (Gizmos/DrawSphere v 0.075))
  ([^Vector3 v r]
    (Gizmos/DrawSphere v r)))

(defn gizmo-cube [^Vector3 v ^Vector3 s]
  (Gizmos/DrawWireCube v s)) 



(log "hard.core is here")

