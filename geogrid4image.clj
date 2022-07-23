(ns geogrid4image
  "Grayscale images as geographic grids and vice versa"
  (:use geogrid
        geoprim))

(defrecord
    imagegrid
    [norwes-point
     ^java.awt.image.BufferedImage
     image
     ^double
     eas-res
     ^double
     sou-res]
  grid
  (dimension-pix
    [_]
    [(long
       (.getWidth
         image))
     (long
       (.getHeight
         image))])
  (eassou-res
    [_]
    [eas-res
     sou-res])
  (corner
    [_]
    norwes-point)
  (data
    [_]
    (->>
      image
      .getData
      .getDataBuffer
      .getData
      (into-array
         Double/TYPE)))
  (subregion
    [imagegrid
     region]
    (let [{:keys [crop-region-pixel-offsets
                  overruns]}   (adjusted-crop-region-to-grid
                                 region
                                 imagegrid)
          {:keys [^long
                  start-x
                  ^long
                  ended-x
                  ^long
                  start-y
                  ^long
                  ended-y]}    crop-region-pixel-offsets
          crop-width           (inc
                                 (-
                                   ended-x
                                   start-x))
          crop-height          (inc
                                 (-
                                   ended-y
                                   start-y))
          subregion-image      (java.awt.image.BufferedImage.
                                 crop-width
                                 crop-height
                                 (.getType
                                   image))
          subregion-graphics2d (.createGraphics
                                 subregion-image)
          [^double
           grid-corner-eas
           ^double
           grid-corner-sou]    (as-eassou
                                 norwes-point)
          cropped-corner       (point-eassou
                                 (+
                                   grid-corner-eas ;;double
                                   (*
                                     start-x
                                     eas-res)) ;; long (integ
                                 (+
                                   grid-corner-sou
                                   (*
                                     start-y
                                     sou-res)))]
      (.drawImage
        subregion-graphics2d
        image
        nil
        (->
          start-x
          -
          int)
        (->
          start-y
          -
          int))
      ;; TODO: Remove
      #_
      (->> ;; save intermediary image to file (might look all black)
        "subregion.png"
        java.io.File. 
        (javax.imageio.ImageIO/write
          subregion-image
          "png"))
      (->imagegrid
        cropped-corner ;; TODO FIX
        subregion-image
        eas-res
        sou-res))))

(defn
  read-file
  "Read in an image file into a grid.
  Default position is for a global map (ie. at `90.-180`)"
  ([^String
    filestr
    eas-res
    sou-res]
   (read-file
     filestr
     eas-res
     sou-res
     (point-eassou
       0
       0))
   )
  ([^String
    filestr
    eas-res
    sou-res
    grid-position]
   (->imagegrid
     grid-position
     (->
       filestr
       java.io.File. 
       javax.imageio.ImageIO/read)
     eas-res
     sou-res)))
#_
(geogrid4image/read-file
  "/home/kxygk/Projects/geogrid4image/rain-2011-03.tif"
  0.1
  0.1
  (geoprim/point-eassou
    0
    0))

#_
(defn
  grid-to-image
  [grid]
  (data
    grid))
