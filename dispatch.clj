(ns hard.dispatch
  (:require [clojure.walk]
    [clojure.string])
  (import [UnityEngine Debug Vector3 Vector2 Color]))

(def FN->QUOTE (atom {}))
(def HASH->FN (atom {}))
(def DISPATCHMAP (atom {}))
 
(defn CLEAN [] (reset! DISPATCHMAP {}))

(defn iter-map [m f]
  (loop [kvs (seq m)]
    (if (empty? kvs) nil 
      (or (f (first kvs))
          (recur (rest kvs))))))

(defn search-variants [m args]
  (iter-map m
    (fn [[k v]] 
      ;TODO try catch
      (if (not (some #(or (false? %) (nil? %))  
        (map 
          #(if (nil? %1) true (%1 %2)) 
          k args)))
        ;dispatchee
        (apply v args)))))


; walk simple compositions to allow equiv
 
(defn cast-from [form col] 
  (cond (vector? form) (vec col) :else col))

(defn -hashed-form [form]
  (cond (sequential? form)
        (cast-from form (map -hashed-form form))
        :else (hash form)))

(defmacro hashed-form [form]
  (let [res (-hashed-form form)] `(quote ~res)))
 

 
(defn unique-fn [f hashed quoted]
  (or (get @HASH->FN hashed)
    (do 
      (swap! HASH->FN assoc hashed f)
      (swap! FN->QUOTE assoc f quoted) f)))

(defn invoke-pass [sym pass args]
  (if-let [domain (get-in @DISPATCHMAP [sym (count args) pass])]
    (search-variants domain args)))

(defmacro extern-resolved-vec [sym]
  (let [extern? (re-find #".+\/.+" (str sym))
        res (resolve (symbol (str sym)))]
    `(do [~extern? ~res])))
  
(defmacro -declare [pass sym args & more]
  (let [[spec code] (if (map? (first more)) [(first more)(rest more)] [{} more])
        arity (count args) 
        ;arg vec can use meta predicates if Symbol,Keyword,String or Map
        meta-preds (map (comp :tag meta) args)
        ;pull ordered arg preds from spec map
        spec-preds (map spec args)
        preds (map #(or %1 %2) spec-preds meta-preds)
        non-meta-args (mapv #(with-meta % nil) args)]
    `(do 
      (swap! DISPATCHMAP update-in [(var ~sym) ~arity ~pass]
          (fn [m#] (conj (or m# {}) 
          ;{list of arg predicates (or nil), declared fn}
          {(map unique-fn 
            (list ~@preds) 
            (hashed-form ~preds) 
            (quote ~preds))
           (fn ~non-meta-args ~@code)} )))

        (fn [& args#] 
            (let [res#
              (remove nil? 
              (list
                (invoke-pass (var ~sym) :before args#)
                (invoke-pass (var ~sym) :rule args#)
                (invoke-pass (var ~sym) :after args#)))]
              (cond (empty? res#) nil
                (= 1 (count res#)) (first res#)
                :else res#))) )))

;having three passes for a single invokation is sugar
  
(defmacro before [sym args & more] 
  (if (re-find #".+\/.+" (str sym))
    `(-declare :before ~sym ~args ~@more)
    `(def ~sym (-declare :rule ~sym ~args ~@more))))
(defmacro rule [sym args & more] 
  (if (re-find #".+\/.+" (str sym))
    `(-declare :rule ~sym ~args ~@more)
    `(def ~sym (-declare :rule ~sym ~args ~@more))))
(defmacro after [sym args & more] 
  (if (re-find #".+\/.+" (str sym))
    `(-declare :after ~sym ~args ~@more)
    `(def ~sym (-declare :rule ~sym ~args ~@more))))


(defn humanize [data] (clojure.walk/postwalk #(if (fn? %) (@FN->QUOTE %) %) data))
;(humanize @DISPATCHMAP)
(defn unseq [& more] (flatten (apply conj [] more)))

 
(defn is [x] #(= % x))
(def a every-pred)
(def non #(complement (apply a %&)))
(defn has-key [k] (fn [o] (not= ::nf (get o k ::nf))))

