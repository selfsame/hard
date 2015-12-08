 
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



(defmacro fun-printer [t]
  (let [v (with-meta (gensym "value")
                     {:tag t})
        w (with-meta (gensym "writer")
                     {:tag 'System.IO.TextWriter})]
    `(fn [~v ~w]
       (.Write ~w
               (str "$:) " ~v )))))

(defmacro install-fun-printer
  "Register a printer for t with clojure's print-method"
  [t]
  `(. ~(with-meta 'print-method
                  {:tag 'clojure.lang.MultiFn})
      ~'addMethod ~t (fun-printer ~t)))

