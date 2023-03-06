import chisel3._
import chisel3.util._

class Histeq(params: HistEqParams) extends Module {
    val io = IO(new Bundle {
        val pixIn = Input(UInt(params.depth.W))
        val pixOut = Output(UInt(params.depth.W))
    })

   // val memBusses = Seq.fill(4)(new MemoryBus(params))

    val (pixCount, pixWrap) = Counter(0 until params.maxPix)

    //val countStage = CountStage(params, io.pixIn, memBusses(0))
    //val accumulateStage = AccumulateStage(params, memBusses(1))
    //val mapStage = MapStage(params, io.pixIn, io.pixOut, memBusses(2))
    //val emptyStage = EmptyStage(params, memBusses(3))

    //val memories = MemoryController(params, memBusses)
}
