(ns exfn.helpers)

(defn keyed-collection [col]
  (map vector (iterate inc 0) col))
