(ns com.adgoji.utils.decimal
  (:refer-clojure :exclude [format])
  (:import
   (java.text DecimalFormat DecimalFormatSymbols)))

(defn format
  "Format decimal `num` with dot and two digits."
  [num]
  (let [decimal-format-symbols
        (doto (DecimalFormatSymbols/getInstance)
          (.setDecimalSeparator \.))]
    (-> (DecimalFormat. "0.00" decimal-format-symbols)
        (.format num))))
