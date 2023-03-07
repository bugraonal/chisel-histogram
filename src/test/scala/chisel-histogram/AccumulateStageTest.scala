import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class AccumulateStageTest extends AnyFlatSpec with ChiselScalatestTester {
    val params = HistEqParams(2, 4, 1, 1.0f)

    behavior of "AccumulateStage"
    it should "accumulate the read value and write it to next addr" in {
		test(new AccumulateStage(params)).withAnnotations(Seq(WriteVcdAnnotation)) { dut => 
            for (i <- 0 until params.numPixelVals) {
                dut.io.address.poke(i.U)
                if (i == 0)
                    dut.io.memoryBus.dout.poke((0).U)
                else
                    dut.io.memoryBus.dout.poke((i - 1).U)

                dut.io.memoryBus.w_addr.expect(i.U)
                dut.io.memoryBus.din.expect((0 until i).sum.U)
                if (i > 1) {
                    dut.io.memoryBus.r_addr.expect((i - 1).U)
                    dut.io.cdfMin.bits.expect(1.U)
                }
                dut.clock.step()
            }
        }
    }
}
