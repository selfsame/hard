 
(ns hard.boiled
	(:use hard.core)
	(:require 
	[clojure.string :as string])
  (:require arcadia.core)
  (:import [UnityEngine]))


 (defn hyphen->camel [charseq]
	(let [parted (partition-by #(= \- %) charseq)]
	(apply str (cons (apply str (first parted))
		(map 
		(fn [col]
			(cond (= '(\-) col) nil
				:else (str (string/upper-case (str (first col)))
						(apply str (rest col)))))
		(rest parted))))))

(defn kw->sym [kw]
	(symbol (hyphen->camel (rest (str kw)))))

(defmacro -set! [value -instance & -more]
	(let [instance (if (vector? -instance) (first -instance) -instance)
		more (if (vector? -instance) (rest -instance) -more)]
		(let [mpath (map kw->sym more)]
		`(set! (.. ~instance ~@mpath) ~value))))

(defmacro -get [-instance & -more]
	(let [instance (if (vector? -instance) (first -instance) -instance)
		more (if (vector? -instance) (rest -instance) -more)]
		(let [mpath (map kw->sym more)]
		`(.. ~instance ~@mpath))))