# Chisel Histogram
This project implements histogram equalization and histogram matching for single channel streaming videos. 

## Histogram Equalization
Histogram equalization adjusts the contrast of an image such that the entire color range is used evenly. 

To accomplish this, first the histogram of the frame is extracted. The histogram is the numer of occurances of each color value. 
Using this histogram, each color value is mapped to a new value.

### Architecture
The hardware implementation of histogram equalization is made up of 4 piplined stages:
    - Counting stage: This stage constructs the histogram.
    - Accumulation stage: This stage converts the histogram to non-normalized CDF (cummulative distribution function).
    - Map stage: This stage maps pixel values to new pixel values.
    - Empty stage: This stage empties the memory. 
Each stage works on an individual memory. Upon completion of a single frame, the memory ports are shifted around such that, the next stage uses the previous stage's memory.

![Architecture](https://github.com/bugraonal/chisel-histogram/blob/master/docs/HistEq.drawio.png?raw=true)

### Usage
There is a model implementation of histogram equalization and the individual stages can be tested using the following command:
```
sbt test
```
Note that this project uses sbt version 1.8.0. This can be overwritten for the tests with the following command:
```
sbt test sbt-version <VERSION>
```
The model will use this [image](resources/simple.jpg) produce a [grayscale version](resources/gray.jpg) and [equalized version](resources/hist_out.jpg).

## Progress
- [ ] Histogram Equalization
    - [x] Model
    - [x] Model test
    - [ ] Hardware
         - [x] Stages
         - [ ] Stage tests
         - [ ] Memory Controller
         - [x] Memory Controller test
         - [ ] Top module
         - [ ] Top modle test
- [ ] Histogram Matching
    - [ ] Model
    - [ ] Model test
    - [ ] Hardware
        - [ ] Stages
        - [ ] Stage tests
        - [ ] Memory Controller
        - [ ] Memory Controller test
        - [ ] Top module
        - [ ] Top modle test
        - [ ] ???
