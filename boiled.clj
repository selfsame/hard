 
(ns hard.boiled
	(:require 
	 arcadia.core
   [clojure.string :as string])
  (:import [UnityEngine]))

(def serial-fields (atom {}))

(defprotocol ISerializable
  (serialize [this])
  (deserialize [this s]))

(defn type-name [sym] (.Name ^MonoType (resolve sym)))

(defmacro fun-printer [t]
  (let [v (with-meta (gensym "value") {:tag t})
        w (with-meta (gensym "writer") {:tag 'System.IO.TextWriter})]
    `(fn [~v ~w]
       (.Write ~w (str "#"  ~t " " (serialize ~v))))))

(defmacro install-fun-printer  [t]
  `(. ~(with-meta 'print-method {:tag 'clojure.lang.MultiFn})
      ~'addMethod ~t (fun-printer ~t)))

(defn constructor-arguments [^MonoType t]
  (get @serial-fields t))

(defmacro vector-constructor [t]
  (let [ctor (constructor-arguments (resolve t))
        params (map #(gensym %) ctor)]
    (list 'fn [(vec params)]
       (list* 'list ''new (list 'quote t) params))))

(defmacro install-type-parser [t]
  `(def ~(symbol (str "parse-" (type-name t)))
     (vector-constructor ~t)))

(deftype Bird [^{:volatile-mutable true :tag int} age ^String plumage])
(reset! serial-fields {Bird '[age plumage]})

(extend-protocol ISerializable
  Bird
  (serialize [this] (str "[" (.age this) " " (.plumage this)"]"))
  (deserialize [this s] 
    (let [[a p] (read-string s)]
    (Bird. a p))))

(install-type-parser Bird)
(install-fun-printer Bird)
(read-string (prn-str (hard.boiled.Bird. 7 "feathers")))

(let [go (clone! :sphere)
      c (cmpt+ go ArcadiaState)]
     (rotation! go (rand-vec 360 360 360))
    (set! (.state c) (Bird. 2048 "feathers")))

(use 'arcadia.core)

(comment 
(use 'hard.core)
(import 'ArcadiaState)






(macroexpand-1 '(fun-printer Bird))
(install-fun-printer Bird)
(deserialize (Bird. 0) (serialize (Bird. 56)))

(read-string (prn-str (Bird. 156)))


(deserialize (Bird. 1) "[45]"))


























(defn longest-constructor-signature
  "Returns the longest parameter list for all constructors in type t"
  [^System.MonoType t]
  (->> (.GetConstructors t)
       (map #(.GetParameters ^ConstructorInfo %))
       (sort (fn [a b] (compare (count a)
                                (count b))))
       last))




(defmacro reconstructor [targetsym t]
  (mapv
    (fn [arg]
      `(. ~targetsym
          ~(symbol arg)))
    (constructor-arguments (resolve t))))

(defmacro type-printer [t]
  (let [v (with-meta (gensym "value")  {:tag t})
        w (with-meta (gensym "writer") {:tag 'System.IO.TextWriter})]
    `(fn [~v ~w]
       (.Write ~w (str "#" ~(str t) " " (reconstructor ~v ~t))))))

(defmacro install-type-printer [t]
  `(. ~(with-meta 'print-method {:tag 'clojure.lang.MultiFn})
      ~'addMethod ~t (type-printer ~t)))

(defmacro serializability [types]
  `(do
     ~@(mapcat
         (fn [t]
           [`(install-type-printer ~t)
            `(install-type-parser ~t)])
         types)))







(comment 

(install-type-parser fun.core.fart)


((juxt (comp read-string prn-str) prn) (fun.core.fart. false "bad"))

(read-string "#fun.core.fart [false \"big\"]\r\n")
(prn-str (fun.core.fart. false "bad"))

((juxt (comp read-string prn-str) prn) (Vector3. 1 2 3))

((vector-constructor fun.core.fart) [false "bad"])

(constructor-arguments fun.core.fart))