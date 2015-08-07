(ns hard.dispatch
  (import [UnityEngine Debug Vector3 Vector2 Color]))

(defonce __R (atom {}))
(defonce __TYPEFNS (atom {}))
 
(defn CLEAN [] 
  (reset! __R (atom {}))
  (reset! __TYPEFNS (atom {})))

(defn is [x] #(= % x))

(defn- arity-match [given pattern]
  (every? true?
    (map #(or (nil? %1) (= %1 %2))
       pattern given)))

(defn- arity-matcher [pattern]
  (let [code (map-indexed #(if %2 (list '= (list 'get 'col %1) %2) true) pattern)] ;('type 'x)
    (if-let [arity-fn (get @__TYPEFNS code)]
      arity-fn
      (do
        (swap! __TYPEFNS
        #(conj % {code (eval (list 'fn '[col] (cons 'and code)))}))
        (get @__TYPEFNS code)))))

(defn -invoke [sym args]
  (if-let [arity-entries (get (get @__R sym) (count args))]
    (let [;validly-typed (filter #((first %) (mapv type args)) arity-entries)
          arg-types (mapv type args)
          ;arity-t (filter #(% arg-types))
          ;pull-t (mapcat #(get arity-entries %))
          ;pre-t (filter (fn [[k v]] (try (if (apply k args) true false) (catch Exception e false))))
          ;res (last (first (sequence (comp arity-t pull-t pre-t) (keys arity-entries))))

          res (last (first (sequence 
            (comp 
              (filter #(% arg-types)) 
              (mapcat #(get arity-entries %)) 
              (filter (fn [[k v]] (try (if (apply k args) true false) (catch Exception e false))))) 
            (keys arity-entries))))
          ]
    (when res (apply res args)))))

(defn register [sym arg-len arg-types conditions f] 
  (swap! __R update-in [sym arg-len] #(merge-with merge % {(arity-matcher arg-types) {conditions f}}))
  #(-invoke sym %&))


(defn- make-conditional [args m]
  (let [arg-idxs (into {} (into {} (map-indexed #(vector %2 %1) args)))]
    (list 'fn args
      (cons 'and
        (map (fn [[k v]] 
            (cond (= :when k) v
                  (get arg-idxs k)
                  (list v k))) m)))))

(defmacro rule [name args & more]
  (let [registry @__R
        name-str (str name)
        arg-len (count args)
        arg-types (mapv (comp :tag meta) args)
        -when (if (map? (first more)) (first more))
        code (if -when (rest more) more)
        conditions (make-conditional args (or -when {}))]
    (if (re-find #".+\/.+" name-str)
      `(register (var ~name) ~arg-len ~arg-types ~conditions (fn ~args ~@code))
      `(def ~name (register (var ~name) ~arg-len ~arg-types ~conditions (fn ~args ~@code))))))