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

class MemoryController(params: HistEqParams) extends Module {
    val io = IO(new Bundle{
        val busses = Vec(4, new MemoryIO(params))
        val stageEnd = Input(Bool())
    })

    val (stageCount, sw) = Counter(0 until 4, io.stageEnd)

    val memories = Seq.fill(4){
        SyncReadMem(params.numPixelVals, UInt(params.memoryDepth.W))
    }

    val busses = VecInit.fill(4)(Wire(new MemoryBus(params)))

    for (i <- 0 until 4) {
        busses(i.U +% stageCount).r_addr := io.busses(i).r_addr
        io.busses(i.U +% stageCount).dout := busses(i).dout
        busses(i.U +% stageCount).w_addr := io.busses(i).w_addr
        busses(i.U +% stageCount).w_en := io.busses(i).w_en
        busses(i.U +% stageCount).din := io.busses(i).din
    }

    for (i <- 0 until 4) {
        busses(i).dout := memories(i).read(busses(i).r_addr)
        when (busses(i).w_en) {
            memories(i).write(busses(i).w_addr, busses(i).din)
        }
    }
    
}

object MemoryController {
    def apply(params: HistEqParams, busses: Seq[MemoryBus]) = {
        val mod = new MemoryController(params)
        busses.zip(mod.io.busses).foreach{case (x, y) => y <> x}
    }
}
