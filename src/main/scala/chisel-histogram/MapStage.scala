import chisel3._
import chisel3.util._

object MapStage {
    def apply(params: HistEqParams, pixIn: UInt, cdfMin: Valid[UInt], pixOut: UInt, memoryBus: MemoryBus)  = {
        val mod = Module(new MapStage(params))
        mod.io.pixIn := pixIn
        mod.io.cdfMin <> cdfMin
        pixOut := mod.io.pixOut
        memoryBus <> mod.io.memoryBus
        mod
    }
}

class MapStage(params: HistEqParams) extends Module {
    val io = IO(new Bundle{
        val pixIn = Input(UInt(params.depth.W))
        val cdfMin = Flipped(Valid(UInt(params.memoryDepth.W)))
        val memoryBus = Flipped(new MemoryIO(params))
        val pixOut = Output(UInt(params.depth.W))
    })

    // Save the minimum value at the end of frame
    val cdfMinReg = RegInit(0.U(params.memoryDepth.W))
    when (io.cdfMin.valid) { cdfMinReg := io.cdfMin.bits }

    // Read CDF of input pixel
    io.memoryBus.r_addr := io.pixIn
    val cdf = io.memoryBus.dout
    // don't write
    io.memoryBus.w_en := false.B
    io.memoryBus.w_addr := DontCare
    io.memoryBus.din := DontCare

    // The constant multiplier and division is done by mutliplying
    // by constant and shifting. The constants are calculated in
    // HistEqParams
    
    // This is most likey to be the longest path in the design.
    // Maybe include a FF here.
    // BRAM -> Add/Sub -> Mult -> Shift -> Compare -> Mux
    val calc = ((io.memoryBus.dout - cdfMinReg) *
        params.mapMultiplier.U) >> params.mapShiftWidth.U

    // TODO: See if you can simplfy this to checking the MSBs
    when (calc > params.maxPix.U) { 
        io.pixOut := params.maxPix.U
    } .otherwise {
        io.pixOut := calc
    }

}
