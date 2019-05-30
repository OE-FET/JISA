
<p align="center"><img src="https://i.imgur.com/MUXiBqM.png"/></p>

# JISA - "Because no-one likes LabView"
`JISA` is a library that I created because I really (really really really) do not like LabView. Not to mention they named their language "G" as if it's somehow comparable to C. This hubris cannot and will not stand.

In essence then, the purpose of `JISA` is to act as an alternative (and actually decent) means of creating experimental control systems. It comprises, largely, of three sections:
### 1. Standardised Instrument Control
```kotlin
val smu1 = K2560( TCPIPAddress("192.168.0.2") )  // Keithley 2450
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
val results = ResultList("Voltage", "Current", "Temperature")

for (i in 1..10) {
    results.addData(smu.getVoltage(), smu.getCurrent(), tc.getTemperature())
}

results.output("data.csv")  // Easy output as CSV file
```
### 3. GUI Building Blocks
```kotlin
val params = Fields("Parameters")

val minV = params.addDoubleField("Min V [V]", 0.0)
val maxV = params.addDoubleField("Max V [V]", 60.0)
val numV = params.addIntegerField("No. Steps", 61)

val plot = Plot("Results", "Voltage", "Current")

val grid = Grid("Main Window", params, plot)

grid.addToolbarButton("Start Sweep") {

    // Makes array starting at minV, ending at maxV in numV steps
    val voltages = Util.makeLinearArray(
        minV.get(),   // Start at
        maxV.get(),   // End at
        numV.get()    // No. steps
    )   
    
    // Code to run sweep
    
}

grid.show()
```
![](https://i.imgur.com/afQsknr.png)


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

|Class|Type|Model|Source|JavaDoc|
|-----|----|-----|------|-------|
|`K2400`|SMU|Keithley 2400 Series|[Source](./src/JISA/Devices/K2400.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/K2400.html)|
|`K2450`|SMU|Keithley 2450|[Source](./src/JISA/Devices/K2450.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/K2450.html)|
|`K236`|SMU|Keithley 236|[Source](./src/JISA/Devices/K236.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/K236.html)|
|`K2600B`|SMU|Keithley 2600B Series|[Source](./src/JISA/Devices/K2600B.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/K2600B.html)|
|`K6430`|SMU|Keithley 6430|[Source](./src/JISA/Devices/K6430.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/K6430.html)|
|`K2200`|DC Power Supply|Keithley 2200 Series|[Source](./src/JISA/Devices/K2200.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/K2200.html)|
|`ITC503`|Temperature Controller|Mercury ITC 503|[Source](./src/JISA/Devices/ITC503.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/ITC503.html)|
|`LS336`|Temperature Controller|LakeShore 336|[Source](./src/JISA/Devices/LS336.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/LS336.html)|
|`SR830`|Lock-In Amplifier|Stanford Research Systems SR830|[Source](./src/JISA/Devices/SR830.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/SR830.html)|
|`SR560`|Voltage Pre-Amp|Stanford Research Systems SR560|[Source](./src/JISA/Devices/SR560.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/SR560.html)|
|`IPS120`|Magnet Power Supply|OI Superconducting Magnet Power Supply|[Source](./src/JISA/Devices/IPS120.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/IPS120.html)|
|`ILM200`|Helium Level Meter|OI He Level Meter|[Source](./src/JISA/Devices/ILM200.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/ILM200.html)|

**Currently Implemented Device Types:**

|Abstract Class|Type|Source|JavaDoc|
|--------------|----|------|-------|
|`SMU`|Source-Measure Unit|[Source](./src/JISA/Devices/SMU.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/SMU.html)|
|`MCSMU`|Multi-Channel Source-Measure Unit|[Source](./src/JISA/Devices/MCSMU.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/MCSMU.html)|
|  |  |  |  |
|`DCPower`|DC Power Supply|[Source](./src/JISA/Devices/DCPower.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/DCPower.html)|
|  |  |  |  |
|`TC`|Temperature Controller|[Source](./src/JISA/Devices/TC.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/TC.html)|
|`MSTC`|Multi-Sensor Temperature Controller|[Source](./src/JISA/Devices/MSTC.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/MSTC.html)|
|`MSMOTC`|Multi-Sensor, Multi-Output Temperature Controller|[Source](./src/JISA/Devices/MSMOTC.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/MSMOTC.html)|
|  |  |  |  |
|`LockIn`|Lock-In Amplifier|[Source](./src/JISA/Devices/LockIn.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/LockIn.html)|
|`DPLockIn`|Dual-Phase Lock-In Amplifier|[Source](./src/JISA/Devices/DPLockIn.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/DPLockIn.html)|
|  |  |  |  |
|`VPreAmp`|Voltage Pre-Amplifier|[Source](./src/JISA/Devices/VPreAmp.java)|[JavaDoc](https://oe-fet.github.io/JISA/JISA/Devices/VPreAmp.html)|
