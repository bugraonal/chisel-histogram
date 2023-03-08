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
    val notRead = RegInit(true.B)
    val oldVal = RegNext(io.memoryBus.dout, 0.U)

    // Read current
    io.memoryBus.r_addr := io.pixIn
    val memVal = Mux(notRead, oldVal, io.memoryBus.dout)
    // write to previous incremented value
    io.memoryBus.w_addr := pixReg
    io.memoryBus.din := memVal +& 1.U
    io.memoryBus.w_en := true.B
    // make sure it doesn't read and write the same address
    notRead := false.B
    when (io.pixIn === pixReg) {
        io.memoryBus.r_addr := io.pixIn + 1.U
        notRead := true.B
    }

}
