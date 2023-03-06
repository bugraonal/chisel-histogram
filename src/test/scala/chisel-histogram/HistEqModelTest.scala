import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_imgproc._
import org.bytedeco.javacpp.opencv_imgcodecs._
import org.bytedeco.javacpp.indexer._
import scala.collection.mutable.ArrayBuffer

class HistEqModelTester extends AnyFlatSpec with ChiselScalatestTester {
    
    val img = imread("resources/simple.jpg", IMREAD_GRAYSCALE)
    val img_indexer: UByteIndexer = img.createIndexer()
    val frame = ArrayBuffer.tabulate(img.rows, img.cols){(x,y) => img_indexer.get(x, y)}
    val params = HistEqParams(img.rows, img.cols, 8)

    behavior of "HistEqModel"
    it should "find the histogram of an image" in {
        val histeq = new HistEqModel(params)
        val hist = histeq.getHist(frame)
        for (i <- 0 until params.numPixelVals)
            assert(hist(i) == frame.flatten.count(x => x == i))
        0
    }

    it should "calculate the CDF from PDF" in {
        val histeq = new HistEqModel(params)
        val hist = histeq.getHist(frame)
        val cdf  = histeq.getCDF(hist)
        var sum = 0
        for (i <- 0 until params.numPixelVals) {
            sum += hist(i)
            assert(cdf(i) == sum, s"CDF index: $i")
        }
        0
    }

    it should "equalize histogram to be wide and flat" in  {
        val histeq = new HistEqModel(params)
        val hist = histeq.getHist(frame)
        val equalized = histeq.equalize(frame)
        val equalizedHist = histeq.getHist(equalized)
        assert(equalizedHist.find(x => x != 0).get <= hist.find(x => x != 0).get)
        assert(equalizedHist.findLast(x => x != 0).get >= hist.findLast(x => x != 0).get)
        val nonZeros = hist.count(x => x != 0)
        val equalizedNonZeros = equalizedHist.count(x => x != 0)
        val histAvg = hist.sum / nonZeros
        val equalizedHistAvg = equalizedHist.sum / equalizedNonZeros
        val histDiff = hist.filter(x => x != 0).map(x => (x - histAvg).abs).sum
        val equalizedHistDiff = equalizedHist.filter(x => x != 0).map(x => (x - equalizedHistAvg).abs).sum
        assert(equalizedHistDiff <= histDiff)
        val img_out = new Mat(params.numRows, params.numCols, CV_8U)
        val img_out_indexer: UByteIndexer = img_out.createIndexer()
        for (i <- 0 until params.numRows) {
            for (j <- 0 until params.numCols) {
                img_out_indexer.put(i.toLong, j.toLong, equalized(i)(j))
            }
        }
        imwrite("resources/gray.jpg", img)
        imwrite("resources/hist_out.jpg", img_out)
        0
    }
}
