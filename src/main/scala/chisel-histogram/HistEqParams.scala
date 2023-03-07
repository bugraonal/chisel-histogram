import chisel3.util._
import scala.math.pow

case class HistEqParams(val numRows: Int, val numCols: Int,
                        val depth: Int, val multiplierError:Float) {
    val numPixels = numRows * numCols
    val numPixelVals = 1 << depth
    val maxPix = numPixelVals - 1
    val memoryDepth = log2Ceil(numPixels)
    val (mapMultiplier, mapShiftWidth) = findMultiplier(multiplierError)

    def findMultiplier(maxError: Float): (Long, Int) = {
        // Find the minimum amount of bits required to keep the fixed point
        // mutliplier error within the maxError parameter
        val maxBits = 32
        val mapMultiplierActual = maxPix.toFloat / numPixels
        for (bits <- 0 to maxBits) {
            val mapMultiplier = (mapMultiplierActual * pow(2, bits)).round
            for (cdf <- 0 to numPixels) {
                // Multiply with maxPix for worst case
                val calculated = (cdf * maxPix * mapMultiplier) >>> bits
                val actual = (cdf * maxPix * mapMultiplierActual)
                val error = (calculated - actual).abs
                if (error < actual * maxError) {
                    println(s"Found multiplier shift width at $bits bits for $maxError error")
                    return (mapMultiplier, bits)
                }
            }
        }
        assert(false, s"Error: Could not find multiplier with $maxError error")
        (0, 0)
    }
}

object HistEqParams {
    
    def apply(rows: Int, cols: Int, depth: Int, multiplierError: Float = 0.1f) = { new HistEqParams(rows, cols, depth, multiplierError) }
}
