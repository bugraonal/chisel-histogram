import chisel3._
import chisel3.util._

class Histeq(params: HistEqParams) extends Module {
    val io = IO(new Bundle {
        val pixIn = Input(UInt(params.depth.W))
        val pixOut = Output(UInt(params.depth.W))
    })

    val memBusses = Seq.fill(4)(new MemoryBus(params))

    // FIXME: Think about the last cycle, see if we need to increase the range
    // by 1 to account for the last pixel
    val (pixCount, pixWrap) = Counter(0 until params.maxPix)
    val cdfMin = Valid(UInt(params.depth.W))
    val isFirstPix = pixCount === 0.U

    val countStage = CountStage(params, io.pixIn, memBusses(0))
    val accumulateStage = AccumulateStage(params, memBusses(1), pixCount)
    val mapStage = MapStage(params, io.pixIn, cdfMin, io.pixOut, memBusses(2))
    val emptyStage = EmptyStage(params, memBusses(3), pixCount)

    val memories = MemoryController(params, memBusses, pixWrap, isFirstPix)
}
