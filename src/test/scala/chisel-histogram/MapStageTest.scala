import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_imgproc._
import org.bytedeco.javacpp.opencv_imgcodecs._
import org.bytedeco.javacpp.indexer._
import scala.collection.mutable.ArrayBuffer

class MapStageTest extends AnyFlatSpec with ChiselScalatestTester {
    val img = imread("resources/simple.jpg", IMREAD_GRAYSCALE)
    val img_indexer: UByteIndexer = img.createIndexer()
    val frame = ArrayBuffer.tabulate(img.rows, img.cols){(x,y) => img_indexer.get(x, y)}
    val params = HistEqParams(img.rows, img.cols, 8, 0.1f)

    behavior of "MapStage"
    it should "Map pixel value correctly" in {
		test(new MapStage(params)).withAnnotations(Seq(WriteVcdAnnotation)) { dut => 
            val histeq = new HistEqModel(params)
            val hist = histeq.getHist(frame)
            val cdf  = histeq.getCDF(hist)
            val cdfMin = cdf.find(x => x != 0).get
            dut.io.cdfMin.bits.poke(cdfMin.U)
            dut.io.cdfMin.valid.poke(true.B)
            dut.clock.step()
            for (i <- 0 to params.maxPix) {
                val expected = histeq.mapPix(i, cdf)
                dut.io.pixIn.poke(i.U) // not really neccessary
                dut.clock.step()
                dut.io.memoryBus.dout.poke(cdf(i))
                val out = dut.io.pixOut.peek()
                val error = (out.litValue - expected).abs.toFloat / params.maxPix
                assert(error <= params.multiplierError, s"Error at $error is greater than expected at $params.multiplierError")
            }
        }
    }
}
