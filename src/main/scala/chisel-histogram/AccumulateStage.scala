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
        val memoryBus = Flipped(new MemoryBus(params))
        val cdfMin = Valid(UInt(params.depth.W))

    })
    
    val accReg = RegInit(0.U(params.memoryDepth.W))

    io.memoryBus.r_addr := io.address + 1.U
    io.memoryBus.w_addr := io.address

    accReg := accReg +& io.memoryBus.dout
    io.memoryBus.din := accReg

    io.cdfMin.valid := io.address === params.maxPix.U
    when (io.address === params.maxPix.U) { cdfInit := false.B }
    val cdfMinReg = Reg(UInt(params.depth.W))
    val cdfInit = Reg(Bool())
    when (~cdfInit & io.memoryBus.din =/= 0.U) {
        io.cdfMin.bits := io.address - 1.U
        cdfInit := true.B
    }
    
}
