import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class EmptyStageTest extends AnyFlatSpec with ChiselScalatestTester {
    val params = HistEqParams(2, 4, 1, 1.0f)

    behavior of "EmptyStage"
    it should "should always write 0 to memory" in {
		test(new EmptyStage(params)).withAnnotations(Seq(WriteVcdAnnotation)) { dut => 
            for (i <- 0 until params.numPixelVals) {
                dut.io.address.poke(i.U)
                dut.io.memoryBus.din.expect(0.U)
                dut.io.memoryBus.w_addr.expect(i.U)
                dut.io.memoryBus.w_en.expect(true.B)
                dut.clock.step()
            }
        }
    }
}
