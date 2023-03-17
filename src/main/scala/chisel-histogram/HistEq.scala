import chisel3._
import chisel3.util._

class HistEq(params: HistEqParams) extends Module {
    val io = IO(new Bundle {
        val pixIn = Input(UInt(params.depth.W))
        val pixOut = Output(UInt(params.depth.W))
        val memBusses = Output(Vec(4, new MemoryBus(params)))
    })

    val memBusses = Seq.fill(4)(Wire(new MemoryBus(params)))
    io.memBusses.zip(memBusses).foreach{
        case (x, y) => x.din := y.din;
                       x.dout := y.dout;
                       x.r_addr := y.r_addr;
                       x.w_addr := y.w_addr;
                       x.w_en := y.w_en
    }

    // FIXME: Think about the last cycle, see if we need to increase the range
    // by 1 to account for the last pixel
    val (pixCount, pixWrap) = Counter(0 to params.numPixels)
    val cdfMin = Wire(Valid(UInt(params.depth.W)))
    val isFirstPix = pixCount === 0.U

    val countStage = CountStage(params, io.pixIn, memBusses(0))
    val accumulateStage = AccumulateStage(params, memBusses(1), pixCount, cdfMin)
    val mapStage = MapStage(params, io.pixIn, cdfMin, io.pixOut, memBusses(2))
    val emptyStage = EmptyStage(params, memBusses(3), pixCount)

    val memories = MemoryController(params, memBusses, pixWrap, isFirstPix)
}
