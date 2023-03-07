import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class MemoryControllerTest extends AnyFlatSpec with ChiselScalatestTester {
    val params = HistEqParams(2, 4, 1, 1.0f)

    def write(cont: MemoryController, mem: Int, value: Int, addr: Int): Unit = {
        cont.io.busses(mem).w_addr.poke(addr.U)
        cont.io.busses(mem).din.poke(value.U)
        cont.io.busses(mem).w_en.poke(true.B)
    }
    def read(cont: MemoryController, mem: Int, addr: Int): Unit = {
        cont.io.busses(mem).r_addr.poke(addr.U)
    }

    behavior of "MemoryController"
    it should "write to and read from all memories" in {
		test(new MemoryController(params)).withAnnotations(Seq(WriteVcdAnnotation)) { dut => 
            dut.io.stageEnd.poke(false.B)
            for (i <- 0 until 2) {
                for (j <- 0 until 4) {
                    write(dut, j, 4 * i + j, i)
                }
                dut.clock.step()
            }
            for (i <- 0 until 2) {
                for (j <- 0 until 4) {
                    dut.io.busses(j).w_en.poke(false.B)
                    read(dut, j, i)
                }
                dut.clock.step()
                for (j <- 0 until 4) {
                    if (i > 0)
                        dut.io.busses(j).dout.expect((4 * i + j).U)
                }
            }
        }
        0
    }

    it should "shift the memory busses" in {
		test(new MemoryController(params)).withAnnotations(Seq(WriteVcdAnnotation)) { dut => 
            dut.io.stageEnd.poke(false.B)
            for (i <- 0 until 2) {
                for (j <- 0 until 4) {
                    write(dut, j, 4 * i + j, i)
                }
                dut.clock.step()
            }
            dut.io.stageEnd.poke(true.B)
            dut.clock.step()
            dut.io.stageEnd.poke(false.B)
            for (i <- 0 until 2) {
                for (j <- 0 until 4) {
                    dut.io.busses(j).w_en.poke(false.B)
                    read(dut, j, i)
                }
                dut.clock.step()
                for (j <- 0 until 4) {
                    if (i > 0) {
                        val sel = (j + 1) % 4
                        dut.io.busses(sel).dout.expect((4 * i + j).U)
                    }
                }
            }
        }
        0

    }
}
