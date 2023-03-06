import chisel3._

object EmptyStage {
    def apply(params: HistEqParams, memoryBus: MemoryBus, address: UInt) = {
        val mod = new EmptyStage(params)
        mod.io.memoryBus <> memoryBus
        mod.io.address := address
    }
}

class EmptyStage(params: HistEqParams) extends Module {
    val io = IO(new Bundle{
        val address = Input(UInt(params.depth.W))
        val memoryBus = Flipped(new MemoryBus(params))
    })

    io.memoryBus.w_addr := io.address
    io.memoryBus.din := 0.U
}
