import chisel3._
import chisel3.util._

class MemoryIO(params: HistEqParams) extends Bundle {
    val r_addr = Input(UInt(params.depth.W))
    val dout = Output(UInt(params.memoryDepth.W))
    val w_addr = Input(UInt(params.depth.W))
    val w_en = Input(Bool())
    val din = Input(UInt(params.memoryDepth.W))
}

class MemoryBus(params: HistEqParams) extends Bundle {
    val r_addr = UInt(params.depth.W)
    val dout = UInt(params.memoryDepth.W)
    val w_addr = UInt(params.depth.W)
    val w_en = Bool()
    val din = UInt(params.memoryDepth.W)
}

object MemoryController {
    def apply(params: HistEqParams, busses: Seq[MemoryBus],
              stageEnd: Bool, stageStart: Bool) = {
        val mod = new MemoryController(params)
        busses.zip(mod.io.busses).foreach{case (x, y) => y <> x}
        mod.io.stageEnd := stageEnd
        mod.io.stageStart := stageStart
        mod
    }
}

class MemoryController(params: HistEqParams) extends Module {
    val io = IO(new Bundle{
        val busses = Vec(4, new MemoryIO(params))
        val stageEnd = Input(Bool())
        val stageStart = Input(Bool())
    })

    // Counter for shifting the memories
    val (stageCount, sw) = Counter(0 until 4, io.stageEnd)

    // memory instances
    val memories = Seq.fill(4){
        SyncReadMem(params.numPixelVals, UInt(params.memoryDepth.W))
    }
    
    // Connect the ports to memories based on count
    io.busses.foreach(x => x.dout := 0.U) // chisel would complain without this
    for (i <- 0 until 4) {
        val sel = stageCount +% i.U
        // Make the write ports use the previous memory in the first cycle
        // of each stage change.
        val writeSel = Mux(io.stageStart, sel - 1.U, sel)
        io.busses(sel).dout := memories(i).read(io.busses(sel).r_addr)
        when (io.busses(writeSel).w_en) {
            memories(i).write(io.busses(writeSel).w_addr, io.busses(writeSel).din)
        }
    }
    
}

