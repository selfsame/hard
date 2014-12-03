hard
====

clojure-unity fun stuff

hard.core
====
my idea of standard interops

hard.input
====
input interpos

hard.hooks
====
BROKE these components have string fields to call functions in the provided namespace

hard.tween
====
An experimental tweening lib.

```clj
(def t1
  (tween {(find-name "Cube") 
        {:position [2 1 1] 
         :local-scale [1 2 1]}}
         2.5 "change" :pow5 :+))
(def t2
  (tween {(find-name "Cube") 
        {:position [0 0 0] 
         :local-scale [1 1 1]}}
         1.2 "return" :pow5 :+))

(link t1 t2 t1)

(run t1)
```

calling hard.tween/add the first time creates a "__tween__" gameobject with the tween updater component.



hard.boiled
====
experimental stuff