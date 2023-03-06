import chisel3._
import chisel3.util._

object MapStage {
    def apply(params: HistEqParams, pixIn: UInt, cdfMin: Valid[UInt], pixOut: UInt, memoryBus: MemoryBus)  = {
        val mod = new MapStage(params)
        mod.io.pixIn := pixIn
        mod.io.cdfMin <> cdfMin
        pixOut := mod.io.pixOut
        memoryBus <> mod.io.memoryBus
    }
}

class MapStage(params: HistEqParams) extends Module {
    val io = IO(new Bundle{
        val pixIn = Input(UInt(params.depth.W))
        val cdfMin = Valid(UInt(params.memoryDepth.W))
        val memoryBus = Flipped(new MemoryBus(params))
        val pixOut = Output(UInt(params.depth.W))
    })

    val cdfMinReg = Reg(UInt(params.memoryDepth.W))
    when (io.cdfMin.valid) { cdfMinReg := io.cdfMin.bits }

    io.memoryBus.r_addr := io.pixIn
    val cdf = io.memoryBus.dout

    // The constant multiplier and division is done by mutliplying
    // by constant and shifting. The constants are calculated in
    // HistEqParams
    val calc = ((io.memoryBus.dout - cdfMinReg) *
        params.mapMultiplier.U) >> params.mapShiftWidth.U

    // TODO: See if you can simplfy this to checking the MSBs
    when (calc > params.maxPix.U) { 
        io.pixOut := params.maxPix.U
    } .otherwise {
        io.pixOut := calc
    }

}
