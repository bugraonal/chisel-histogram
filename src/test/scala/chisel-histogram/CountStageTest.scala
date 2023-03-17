import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class CountStageTest extends AnyFlatSpec with ChiselScalatestTester {
    val params = HistEqParams(2, 4, 2, 1.0f)

    behavior of "CountStage"
    it should "should count the pixels" in {
		test(new CountStage(params)).withAnnotations(Seq(WriteVcdAnnotation)) { dut => 
            for (i <- 0 until 2 * params.numPixelVals) {
                dut.io.pixIn.poke((i % params.numPixelVals).U)
                dut.io.memoryBus.dout.poke((i / params.numPixelVals).U)
                if (i != 0) {
                    dut.io.memoryBus.r_addr.expect((i % params.numPixelVals).U)
                    dut.io.memoryBus.w_addr.expect(((i - 1) % params.numPixelVals).U)
                }
                dut.io.memoryBus.din.expect((i / params.numPixelVals + 1).U)
                dut.clock.step()
            }
        }
    }
}
