import scala.collection.mutable.ArrayBuffer

class HistEqModel(params: HistEqParams) {
    def getHist(frame: ArrayBuffer[ArrayBuffer[Int]]): ArrayBuffer[Int] = {
        val hist = ArrayBuffer.fill(params.numPixelVals)(0)
        for (row <- frame) {
            for (pix <- row) {
                hist(pix) = hist(pix) + 1
            }
        }
        hist
    }

    def getCDF(hist: ArrayBuffer[Int]): ArrayBuffer[Int] = {
        val cdf = ArrayBuffer.fill(params.numPixelVals)(0)
        for (i <- 0 until params.numPixelVals) {
            cdf(i) = hist.slice(0, i + 1).sum
        }
        cdf
    }
    
    def mapPix(pix: Int, cdf: ArrayBuffer[Int]): Int = {
        val cdfMin = cdf.find(x => x != 0)
        ((cdf(pix) - cdfMin.get).toFloat / 
            (params.numPixels - cdf.min) * params.maxPix).round
    }

    def equalize(frame: ArrayBuffer[ArrayBuffer[Int]]): ArrayBuffer[ArrayBuffer[Int]] = {
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
