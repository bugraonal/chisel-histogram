import chisel3._
import chisel3.util._

object AccumulateStage {
    def apply(params: HistEqParams, memoryBus: MemoryBus, address: UInt) = {
        val mod = new AccumulateStage(params)
        mod.io.memoryBus <> memoryBus
        mod.io.address := address
        mod
    }
}

class AccumulateStage(params: HistEqParams) extends Module {
    val io = IO(new Bundle{
        val address = Input(UInt(params.depth.W))
        val memoryBus = Flipped(new MemoryIO(params))
        val cdfMin = Valid(UInt(params.depth.W))

    })
    
    val accReg = RegInit(0.U(params.memoryDepth.W))

    io.memoryBus.r_addr := io.address + 1.U
    io.memoryBus.w_addr := io.address

    io.memoryBus.w_en := true.B

    accReg := accReg +& io.memoryBus.dout
    io.memoryBus.din := accReg

    io.cdfMin.valid := io.address === params.maxPix.U
    val cdfMinReg = RegInit(0.U(params.depth.W))
    val cdfInit = RegInit(false.B)
    when (io.address === params.maxPix.U) { cdfInit := false.B }
    io.cdfMin.bits := cdfMinReg
    when (~cdfInit & io.memoryBus.din =/= 0.U) {
        cdfMinReg := io.address - 1.U
        cdfInit := true.B
    }
    
}
