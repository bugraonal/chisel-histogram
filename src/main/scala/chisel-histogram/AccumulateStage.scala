import chisel3._
import chisel3.util._

object AccumulateStage {
    def apply(params: HistEqParams, memoryBus: MemoryBus, address: UInt, cdfMin: Valid[UInt]) = {
        val mod = Module(new AccumulateStage(params))
        mod.io.memoryBus <> memoryBus
        mod.io.address := address
        cdfMin <> mod.io.cdfMin
        mod
    }
}

class AccumulateStage(params: HistEqParams) extends Module {
    val io = IO(new Bundle{
        val address = Input(UInt(params.memoryDepth.W))
        val memoryBus = Flipped(new MemoryIO(params))
        val cdfMin = Valid(UInt(params.depth.W))
    })

    // Register to store accumulated value
    val accReg = RegNext(io.memoryBus.din, 0.U)
    val addressReg = RegNext(io.address, params.maxPix.U)
    when (addressReg > params.maxPix.U) { accReg := 0.U }

    // Read current addr, write to prev
    io.memoryBus.r_addr := io.address

    io.memoryBus.w_addr := addressReg
    io.memoryBus.w_en := addressReg <= params.maxPix.U && io.address > 0.U
    io.memoryBus.din := accReg +& io.memoryBus.dout

    // Minimum CDF address where the result is non-zero
    // Only set valid at the end of the pixel count
    io.cdfMin.valid := io.address === (params.numPixels - 1).U
    val cdfMinReg = RegInit(0.U(params.depth.W))
    val cdfInit = RegInit(false.B)
    when (io.address === (params.numPixels - 1).U) {
        cdfInit := false.B
    }
    io.cdfMin.bits := cdfMinReg
    when (~cdfInit & io.memoryBus.din =/= 0.U) {
        cdfMinReg := io.address - 1.U
        cdfInit := true.B
    }

}
