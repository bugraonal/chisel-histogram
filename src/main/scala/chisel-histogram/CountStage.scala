import chisel3._

object CountStage {
    def apply(params: HistEqParams, pixIn: UInt, memoryBus: MemoryBus) = {
        val mod = Module(new CountStage(params))
        mod.io.pixIn := pixIn
        mod.io.memoryBus.dout := memoryBus.dout
        memoryBus.din := mod.io.memoryBus.din
        memoryBus.w_en := mod.io.memoryBus.w_en
        memoryBus.r_addr := mod.io.memoryBus.r_addr
        memoryBus.w_addr := mod.io.memoryBus.w_addr
        mod
    }
}

class CountStage(params: HistEqParams) extends Module {
    val io = IO(new Bundle {
        val pixIn = Input(UInt(params.depth.W))
        val memoryBus = Flipped(new MemoryIO(params))
    })
    
    // Delay the pixel value by 1-cycle to write back
    val pixReg = RegNext(io.pixIn)

    // Read current
    io.memoryBus.r_addr := io.pixIn
    // write to previous incremented value
    io.memoryBus.w_addr := pixReg
    io.memoryBus.din := io.memoryBus.dout + 1.U
    // Just so we can tell it to not write in cycle 0
    io.memoryBus.w_en := RegNext(true.B, false.B)

}
