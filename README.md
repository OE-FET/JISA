
<p align="center"><img src="https://i.imgur.com/MUXiBqM.png"/></p>

# JISA - "Because no-one likes LabView"
`JISA` is a library that I created because I really (really really really) do not like LabView. Not to mention they named their language "G" as if it's somehow comparable to C. This hubris cannot and will not stand.

In essence then, the purpose of `JISA` is to act as an alternative (and actually decent) means of creating experimental control systems. It comprises, largely, of three sections:
### 1. Standardised Instrument Control
```kotlin
// Connect to instruments
val smu1 = K2450( TCPIPAddress("192.168.0.2") )  // Keithley 2450
val smu2 = K236( GPIBAdrress(0,17) )             // Keithley 236

smu1.useAutoRanges()
smu1.setCurrentLimit(10e-3)
smu1.setVoltage(5.0)
smu1.turnOn()

smu2.useAutoRanges()           // Same code, despite different SMU
smu2.setCurrentLimit(10e-3)
smu2.setVoltage(5.0)
smu2.turnOn()
```
### 2. Data Handling
```kotlin
// Create results storage
val results = ResultList("Voltage", "Current", "Temperature")

// Take 10 readings
repeat(10) {
    results.addData(smu.getVoltage(), smu.getCurrent(), tc.getTemperature())
}

// Easy output as CSV file
results.output("data.csv")  
```
### 3. GUI Building Blocks
```kotlin
// Create user-input panel
val params = Fields("Parameters")

// Add input fields to it
val minV = params.addDoubleField("Min V [V]", 0.0)
val maxV = params.addDoubleField("Max V [V]", 60.0)
val numV = params.addIntegerField("No. Steps", 61)

// Create plot
val plot = Plot("Results", "Voltage", "Current")

// Add panel and plot to a grid
val grid = Grid("Main Window", params, plot)

// Add start button to toolbar
grid.addToolbarButton("Start Sweep") {

    // Makes array starting at minV, ending at maxV in numV steps
    val voltages = Util.makeLinearArray(
        minV.get(),   // Start at
        maxV.get(),   // End at
        numV.get()    // No. steps
    )   
    
    // Code to run sweep
    
}

// Show the grid in a window
grid.show()
```
![](https://i.imgur.com/5aO8bGQ.png)


## JISA the Polyglot
`JISA`  is written in Java, but because Java is all about magic beans, you can actually use it in almost any language. More specifically, any language that can either be compiled to java byte-code or interpreted by a Java program (so basically anything) can use the library.

For example, here's the same program written in Java, Kotlin, Python and even gosh-darn MATLAB:

**Java - Classic style, fast, robust but strict**
```java
public class Main {

    public static void main(String[] args) throws Exception {

        SMU         smu     = new K2450(new GPIBAddress(0, 20));
        ResultTable results = new ResultList("Voltage", "Current");

        results.setUnits("V", "A");

        smu.setVoltage(0.0);
        smu.turnOn();

        for (double v : Util.makeLinearArray(0, 60, 61)) {

            smu.setVoltage(v);
            Util.sleep(500);
            results.addData(smu.getVoltage(), smu.getCurrent());

        }

        smu.turnOff();
        results.output("data.csv");

    }

}
```
**Kotlin - Simple, concise and fast - great for beginners**
```kotlin
fun main() {

    val smu     = K2450(GPIBAddress(0,20))
    val results = ResultList("Voltage", "Current")

    results.setUnits("V", "A")

    smu.setVoltage(0.0)
    smu.turnOn()

    for (v in Util.makeLinearArray(0.0, 60.0, 61)) {
    
        smu.setVoltage(v)
        Util.sleep(500)
        results.addData(smu.getVoltage(), smu.getCurrent())
        
    }
    
    smu.turnOff()
    results.output("data.csv")

}
```
**Python (Jython) - Simple like Kotlin, but slooooow and less robust**
```python
def main():
    
    smu     = K2450(GPIBAddress(0,20))
    results = ResultList(["Voltage", "Current"])
    
    results.setUnits(["V", "A"])   

    smu.setVoltage(0.0)
    smu.turnOn()
    
    for v in Util.makeLinearArray(0.0, 60.0, 61):
    
        smu.setVoltage(v)
        Util.sleep(500)
        results.addData([smu.getVoltage(), smu.getCurrent()])
    
    
    smu.turnOff()
    results.output("data.csv")


main()
```
**MATLAB - Why?**
```matlab
function main()
    
    smu     = JISA.Devices.K2450(JISA.Addresses.GPIBAddress(0,20));
    results = JISA.Experiment.ResultList({'Voltage', 'Current'});

    results.setUnits({'V', 'A'});    

    smu.setVoltage(0.0);
    smu.turnOn();
    
    for v=JISA.Util.makeLinearArray(0.0, 60.0, 61)
    
        smu.setVoltage(v);
        JISA.Util.sleep(500);
        results.addData([smu.getVoltage(), smu.getCurrent()]);
        
    end
    
    smu.turnOff();
    results.output('data.csv');
    
end
```
We can then extend this program easily, with only two lines, to display a plot of the results as they come in. Taking the example in Kotlin:
```kotlin
fun main() {

    val smu     = K2450(GPIBAddress(0,20))
    val results = ResultList("Voltage", "Current")

    results.setUnits("V", "A")    

    // Make a plot that watches our results
    val plot = Plot("Results", results)
    plot.show()

    smu.setVoltage(0.0)
    smu.turnOn()

    for (v in Util.makeLinearArray(0.0, 60.0, 61)) {
    
        smu.setVoltage(v)
        Util.sleep(500)
        results.addData(smu.getVoltage(), smu.getCurrent())
        
    }
    
    smu.turnOff()
    results.output("data.csv")

}
```
Resulting in:

![](https://i.imgur.com/Rqs9n3R.png)

## Supported Instruments 

**Currently Implemented Devices:**

| [](<>)                 | Model        | Type                        | Class     |
| ---------------------- | ------------ | --------------------------- | --------- |
| **Keithley**           | 236          | SMU (Single-Channel)        | `K236`    |
| [](<>)                 | 2400 Series  | SMU (Single-Channel)        | `K2400`   |
| [](<>)                 | 2450         | SMU (Single-Channel)        | `K2450`   |
| [](<>)                 | 2600B Series | SMU (Multi-Channel)         | `K2600B`  |
| [](<>)                 | 6430         | SMU (Single-Channel)        | `K6430`   |
| [](<>)                 | 2200         | DC Power Supply             | `K2200`   |
| [](<>)                 | 2182         | Voltmeter                   | `K2182`   |
| **Oxford Instruments** | ITC-503      | Temperature Controller      | `ITC503`  |
| [](<>)                 | IPS-120      | Magnet Controller           | `IPS120`  |
| [](<>)                 | ILM-200      | He Level Meter              | `ILM200`  |
| **Lake Shore**         | 336          | Temperature Controller      | `LS336`   |
| **Stanford Research**  | SR830        | Lock-In Amplifier           | `SR830`   |
| [](<>)                 | SR560        | Voltage Pre-Amp             | `SR560`   |
| **Eurotherm**          | 2408         | Temperature Controller      | `ET2408`  |
| **Pico Technology**    | USB-TC08     | Thermometer (Multi-Channel) | `USBTC08` |

**Currently Implemented Device Types:**

|Abstract Class|Type|Source|JavaDoc|
|--------------|----|------|-------|
|`SMU`|Source-Measure Unit|[Source](src/jisa/devices/SMU.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/SMU.html)|
|`MCSMU`|Multi-Channel Source-Measure Unit|[Source](src/jisa/devices/MCSMU.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/MCSMU.html)|
|  |  |  |  |
|`DCPower`|DC Power Supply|[Source](src/jisa/devices/DCPower.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/DCPower.html)|
|  |  |  |  |
|`TC`|Temperature Controller|[Source](src/jisa/devices/TC.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/TC.html)|
|`MSTC`|Multi-Sensor Temperature Controller|[Source](src/jisa/devices/MSTC.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/MSTC.html)|
|`MSMOTC`|Multi-Sensor, Multi-Output Temperature Controller|[Source](src/jisa/devices/MSMOTC.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/MSMOTC.html)|
|  |  |  |  |
|`LockIn`|Lock-In Amplifier|[Source](src/jisa/devices/LockIn.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/LockIn.html)|
|`DPLockIn`|Dual-Phase Lock-In Amplifier|[Source](src/jisa/devices/DPLockIn.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/DPLockIn.html)|
|  |  |  |  |
|`VPreAmp`|Voltage Pre-Amplifier|[Source](src/jisa/devices/VPreAmp.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/VPreAmp.html)|
