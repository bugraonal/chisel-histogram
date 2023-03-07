import chisel3._

object CountStage {
    def apply(params: HistEqParams, pixIn: UInt, memoryBus: MemoryBus) = {
        val mod = new CountStage(params)
        mod.io.pixIn := pixIn
        mod.io.memoryBus <> memoryBus
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

    // Read current, write to previous incremented value
    io.memoryBus.r_addr := io.pixIn
    io.memoryBus.w_addr := pixReg
    io.memoryBus.din := io.memoryBus.dout +& 1.U
    io.memoryBus.w_en := true.B

}
