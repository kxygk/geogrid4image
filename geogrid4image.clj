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
    (let [raster (.getData image)
          w (.getWidth raster)
          h (.getHeight raster)]
      (double-array
        (for [y (range h)
              x (range w)]
          ;; a little goofy..
          ;; but best to run across the pixels
          (.getSampleDouble raster
                            x
                            y
                            0))))
    ;; This is more dangerous b/c underlying type might be `short`
    ;; which leads to negative values
    #_
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
  read-location
  "Read in an image into a grid.
  `input` should be a `File` `URL` `InputStream` etc.
  For our uses it's either:
  - a `File` made from a file name `io/file`
  - a `URL` made from a resource `io/resource`
  Default position is for a global map (ie. at `90.-180`)"
  ([input
    eas-res
    sou-res]
   (read-location
     input
     eas-res
     sou-res
     (point-eassou
       0
       0))
   )
  ([input
    eas-res
    sou-res
    grid-position]
   (let [buffimg (-> input
                     javax.imageio.ImageIO/read)]
     (->imagegrid
       grid-position
       buffimg
       eas-res
       sou-res))))
#_
(geogrid4image/read-file
  "/home/kxygk/Projects/geogrid4image/rain-2011-03.tif"
  0.1
  0.1
  (geoprim/point-eassou
    0
    0))
;; => {:norwes-point {:eas 0.0, :sou 0.0},
;;     :image
;;     #object[java.awt.image.BufferedImage 0x4020bc9 "BufferedImage@4020bc9: type = 11 ColorModel: #pixelBits = 16 numComponents = 1 color space = java.awt.color.ICC_ColorSpace@7ae183bd transparency = 1 has alpha = false isAlphaPre = false ShortInterleavedRaster: width = 3600 height = 1800 #numDataElements 1"],
;;     :eas-res 0.1,
;;     :sou-res 0.1}
#_
(geogrid4image/read-file
  "/home/kxygk/Data/sst/monthly/geotiff-rot/block-0524-rot.tiff"
  0.1
  0.1
  (geoprim/point-eassou
    0
    0))
;; => {:norwes-point {:eas 0.0, :sou 0.0},
;;     :image
;;     #object[java.awt.image.BufferedImage 0x7425462e "BufferedImage@7425462e: type = 11 ColorModel: #pixelBits = 16 numComponents = 1 color space = java.awt.color.ICC_ColorSpace@7ae183bd transparency = 1 has alpha = false isAlphaPre = false ShortInterleavedRaster: width = 1440 height = 720 #numDataElements 1"],
;;     :eas-res 0.1,
;;     :sou-res 0.1}
#_
(geogrid4image/read-file
  "/home/kxygk/Data/era5/monthly/rot/era5-geotiff-block-0120-rot.tiff"
  0.1
  0.1
  (geoprim/point-eassou
    0
    0))
;; => {:norwes-point {:eas 0.0, :sou 0.0},
;;     :image
;;     #object[java.awt.image.BufferedImage 0x57218434 "BufferedImage@57218434: type = 11 ColorModel: #pixelBits = 16 numComponents = 1 color space = java.awt.color.ICC_ColorSpace@7ae183bd transparency = 1 has alpha = false isAlphaPre = false ShortInterleavedRaster: width = 1440 height = 721 #numDataElements 1"],
;;     :eas-res 0.1,
;;     :sou-res 0.1}
#_
(geogrid4image/read-file
"/home/kxygk/Data/era5/era5-geotiff-block-44.tiff"
  0.25
  0.25
  (geoprim/point-eassou
    0
    0))

#_
(defn
  grid-to-image
  [grid]
  (data
    grid))
