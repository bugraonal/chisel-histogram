import scala.collection.mutable.ArrayBuffer

class HistEqModel(params: HistEqParams) {
    def getHist(frame: ArrayBuffer[ArrayBuffer[Int]]): ArrayBuffer[Long] = {
        // Find the histogram of frame
        val hist = ArrayBuffer.fill(params.numPixelVals)(0.toLong)
        for (row <- frame) {
            for (pix <- row) {
                hist(pix) = hist(pix) + 1
            }
        }
        hist
    }

    def getCDF(hist: ArrayBuffer[Long]): ArrayBuffer[Long] = {
        // convert histogram to non-normalized cdf
        val cdf = ArrayBuffer.fill(params.numPixelVals)(0.toLong)
        for (i <- 0 until params.numPixelVals) {
            cdf(i) = hist.slice(0, i + 1).sum
        }
        cdf
    }
    
    def mapPix(pix: Int, cdf: ArrayBuffer[Long]): Int = {
        // map a given pixel value to it's new value
        val cdfMin = cdf.find(x => x != 0).get
        ((cdf(pix) - cdfMin).toFloat / params.numPixels * params.maxPix).round
    }

    def equalize(frame: ArrayBuffer[ArrayBuffer[Int]]): ArrayBuffer[ArrayBuffer[Int]] = {
        // map all the pixels
        val hist = getHist(frame)
        val cdf = getCDF(hist)
        val newFrame = ArrayBuffer.fill(params.numRows, params.numCols)(0)
        for (row <- 0 until params.numRows) {
            for (col <- 0 until params.numCols) {
                val pix = frame(row)(col)
                newFrame(row)(col) = mapPix(pix, cdf)
            }
        }
        newFrame
    }
}
