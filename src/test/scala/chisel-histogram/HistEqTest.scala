import chiseltest._
import chisel3._
import org.scalatest.flatspec.AnyFlatSpec
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_imgproc._
import org.bytedeco.javacpp.opencv_imgcodecs._
import org.bytedeco.javacpp.indexer._
import scala.collection.mutable.ArrayBuffer
import java.io._

class HistEqTest extends AnyFlatSpec with ChiselScalatestTester {
    
    val img = imread("resources/small.jpg", IMREAD_GRAYSCALE)
    val imgIndexer: UByteIndexer = img.createIndexer()
    val frame = ArrayBuffer.tabulate(img.rows, img.cols){(x,y) => imgIndexer.get(x, y)}
    val params = HistEqParams(img.rows, img.cols, 8, 0.5f)
    val model = new HistEqModel(params)
    val hist = model.getHist(frame)
    val cdf = model.getCDF(hist)
    val equalized = model.equalize(frame)
    val equalizedHist = model.getHist(equalized)

    // Simulate an entire frame
    def stream_image(dut: HistEq,
                     image: ArrayBuffer[ArrayBuffer[Int]]
                    ): (Seq[ArrayBuffer[Long]], ArrayBuffer[ArrayBuffer[Int]]) = {
        val memories = Seq.fill(4){
            ArrayBuffer.fill(params.numPixelVals)(0.toLong)
        }
        val out = ArrayBuffer.fill(params.numRows)(ArrayBuffer.fill(params.numCols)(0))
        for ((row, row_ind) <- image.zipWithIndex) {
            for ((pixel, col_ind) <- row.zipWithIndex) {
                dut.io.pixIn.poke(pixel.U)
                step(dut, memories)
                out(row_ind)(col_ind) = dut.io.pixOut.peekInt().toInt
            }
        }
        (memories, out)
    }

    // Create an array with the mirrored contents of the bram
    def mirror_memory(memoryBus: MemoryBus, array: ArrayBuffer[Long]): Unit = {
        if (memoryBus.w_en.peekInt().toInt == 1) {
            array(memoryBus.w_addr.peekInt().toInt) = memoryBus.din.peekInt().toLong
        }
    }

    // Advance sim one cycle while mirroring the memories
    def step(dut: HistEq, memories: Seq[ArrayBuffer[Long]]) = {
        dut.io.memBusses.zip(memories).foreach{
            case (hwMem, mirrorMem) => mirror_memory(hwMem, mirrorMem)
        }
        dut.clock.step()
    }

    // compare two arrays
    def compare_arrays(arr0: ArrayBuffer[Long], arr1: ArrayBuffer[Long]) = { 
        arr0.zip(arr1).forall{case (elem0, elem1) => elem0 == elem1}
    }

    // compare two images with error margin
    def compare_image(img0: ArrayBuffer[ArrayBuffer[Int]], img1: ArrayBuffer[ArrayBuffer[Int]]) = {
        val error = params.maxPix * params.multiplierError
        val diff = img0.flatten.zip(img1.flatten).map{
            case (pix0, pix1) => (pix0 - pix1).abs < error
        }
        // first 2 pixels can be off due to initialization
        diff.count(x => x == false) <= 2
    }

    // write to image
    def write_image(name: String, data: ArrayBuffer[ArrayBuffer[Int]]) = {
        val out = new Mat(img.size, CV_8U)
        val out_indexer: UByteIndexer = out.createIndexer()
        for (row <- 0 until params.numRows) {
            for (col <- 0 until params.numCols) {
                out_indexer.put(row.toLong, col.toLong, data(row)(col))
            }
        }
        imwrite(name, out)
    }

    // write array as plaintext
    def write_array(name: String, data: ArrayBuffer[Long]) = {
        val pw = new PrintWriter(new File(name))
        for (d <- data)
            pw.write(s"$d\n")
        pw.close()
    }

    behavior of "HistEq"
    it should "find the histogram of an image" in {
		test(new HistEq(params)).withAnnotations(Seq(VerilatorBackendAnnotation, WriteVcdAnnotation)) { dut => 
            val (memories, out) = stream_image(dut, frame)
            // Simulate the last cycle
            step(dut, memories)
            write_array("resources/sim_hist.txt", memories(0))
            assert(compare_arrays(hist, memories(0)))
        }
    }
    
    it should "find the cdf of an image" in {
		test(new HistEq(params)).withAnnotations(Seq(VerilatorBackendAnnotation, WriteVcdAnnotation)) { dut => 
            stream_image(dut, frame)
            // CDF if calculated in the second frame
            val (memories, out) = stream_image(dut, frame)
            // Simulate the last cycle
            step(dut, memories)
            write_array("resources/sim_cdf.txt", memories(1))
            assert(compare_arrays(cdf, memories(1)))
        }
    }

    it should "find the equalized image" in {
		test(new HistEq(params)).withAnnotations(Seq(VerilatorBackendAnnotation, WriteVcdAnnotation)) { dut => 
            stream_image(dut, frame)
            stream_image(dut, frame)
            // output becomes valid only in the third frame
            val (memories, out) = stream_image(dut, frame)
            write_image("resources/sim_out.jpg", out)
            write_image("resources/model_out.jpg", equalized)
            val sim_out_hist = model.getHist(out)
            write_array("resources/model_hist.txt", equalizedHist)
            write_array("resources/sim_hist.txt", sim_out_hist)
            assert(compare_image(equalized, out))
        }
    }
}

