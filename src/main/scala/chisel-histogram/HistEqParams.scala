import chisel3.util._

case class HistEqParams(val numRows: Int, val numCols: Int, val depth: Int) {
    val numPixels = numRows * numCols
    val numPixelVals = 1 << depth
    val maxPix = numPixelVals - 1
    val memoryDepth = log2Ceil(numPixels)
    // TODO: write a function to find these constants
    val mapMultiplier = 324234
    val mapShiftWidth = 10
}

object HistEqParams {
    
    def apply(rows: Int, cols: Int, depth: Int) = { new HistEqParams(rows, cols, depth) }
}
