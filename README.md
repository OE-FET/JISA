[![](https://img.shields.io/badge/Download-JISA-teal)](https://github.com/OE-FET/JISA/raw/master/JISA.jar)

<p align="center"><img src="https://i.imgur.com/MUXiBqM.png"/></p>

# JISA - "Because no-one likes LabView"
`JISA` is a library that I created, here in the Cavendish Laboratory, because I really (really really really) do not like LabView. Not to mention they named their language "G" as if it's somehow comparable to C. This hubris cannot and will not stand.

In essence then, the purpose of `JISA` is to act as an alternative (and actually decent) means of creating experimental control systems. It comprises, largely, of three sections:
### 1. Standardised Instrument Control

`JISA` implements standard interfaces for each "type" of instrument, meaning that instruments are easily interchangeable. If we connect to a Keithley SMU and an Agilent SPA

```kotlin
// Connect to instruments
val keithley = K2600B(TCPIPAddress("192.168.0.5"))
val agilent  = Agilent4155X(GPIBAddress(20))
```

then `JISA` simply represents them as collections of `SMU` channels

```kotlin
// Get first channel from both instruments
val smuK = keithley.getChannel(0)
val smuA = agilent.getChannel(0)
```

meaning that operating them is done exactly the same way in JISA regardless of which make/model of instsrument they are from

```kotlin
smuK.setIntegrationTime(0.1)     // Set the integration time
smuK.useAutoRanges()             // Use auto ranging on current and voltage
smuK.setVoltage(5.0)             // Set to source 5.0 V
smuK.turnOn()                    // Enable output of channel
val currentK = smuK.getCurrent() // Measure current
smuK.turnOff()                   // Disable output of channel

smuA.setIntegrationTime(0.1)     // Set the integration time
smuA.useAutoRanges()             // Use auto ranging on current and voltage
smuA.setVoltage(5.0)             // Set to source 5.0 V
smuA.turnOn()                    // Enable output of channel
val currentA = smuA.getCurrent() // Measure current
smuA.turnOff()                   // Disable output of channel
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

    // Makes range starting at minV, ending at maxV in numV steps
    val voltages = Range.linear(
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

**Java - Classic style, robust but verbose, like a northern grandparent.**
```java
public class Main {

    public static void main(String[] args) throws Exception {

        SMU         smu     = new K2450(new GPIBAddress(0, 20));
        ResultTable results = new ResultList(new Col("Voltage", "V"), new Col("Current", "A"));

        smu.setVoltage(0.0);
        smu.turnOn();

        for (double v : Range.linear(0, 60, 61)) {

            smu.setVoltage(v);
            Util.sleep(500);
            results.addData(smu.getVoltage(), smu.getCurrent());

        }

        smu.turnOff();
        results.output("data.csv");

    }

}
```
**Kotlin - Slick, simplified and concise without rocking the boat**
```kotlin
fun main() {

    val smu     = K2450(GPIBAddress(0,20))
    val results = ResultList(Col("Voltage", "V"), Col("Current", "A"))

    smu.setVoltage(0.0)
    smu.turnOn()

    for (v in Range.linear(0.0, 60.0, 61)) {
    
        smu.setVoltage(v)
        Util.sleep(500)
        results.addData(smu.getVoltage(), smu.getCurrent())
        
    }
    
    smu.turnOff()
    results.output("data.csv")

}
```
**Python (Jython) - "Screw your traditions, I'm a snake from the future"**

To use in Python, take a look at PyJISA [here](https://github.com/OE-FET/PyJISA).

```python
def main():
    
    smu     = K2450(GPIBAddress(0,20))
    results = ResultList([Col("Voltage", "V"), Col("Current", "A")])

    smu.setVoltage(0.0)
    smu.turnOn()
    
    for v in Range.linear(0.0, 60.0, 61):
    
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
    
    smu     = jisa.devices.K2450(JISA.Addresses.GPIBAddress(0,20));
    results = jisa.experiment.ResultList({'Voltage', 'Current'});

    results.setUnits({'V', 'A'});    

    smu.setVoltage(0.0);
    smu.turnOn();
    
    for v=jisa.maths.Range.linear(0.0, 60.0, 61)
    
        smu.setVoltage(v);
        jisa.Util.sleep(500);
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
    val results = ResultList(Col("Voltage", "V"), Col("Current", "A")) 

    // Make a plot that watches our results
    val plot = Plot("Results", results)
    plot.show()

    smu.setVoltage(0.0)
    smu.turnOn()

    for (v in Range.linear(0.0, 60.0, 61)) {
    
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

| [](<>)                 | Model        | Type                        | Class           |
| ---------------------- | ------------ | --------------------------- | --------------- |
| **Keithley**           | 236          | SMU (Single-Channel)        | `K236`          |
| [](<>)                 | 2400 Series  | SMU (Single-Channel)        | `K2400`         |
| [](<>)                 | 2450         | SMU (Single-Channel)        | `K2450`         |
| [](<>)                 | 2600B Series | SMU (Multi-Channel)         | `K2600B`        |
| [](<>)                 | 6430         | SMU (Single-Channel)        | `K6430`         |
| [](<>)                 | 2200         | DC Power Supply             | `K2200`         |
| [](<>)                 | 2182         | Voltmeter                   | `K2182`         |
| **Agilent / Keysight** | 4155B/C      | SPA / SMU (Multi-Channel)   | `Agilent4155X`  |
| [](<>)                 | 4156B/C      | SPA / SMU (Multi-Channel)   | `Agilent4156X`  |
| [](<>)                 | B1500A       | SPA / SMU (Multi-Channel)   | `AgilentB1500A` |
| [](<>)                 | E3644A       | DC Power Supply             | `AgilentE3644A` |
| **AIM-TTI**            | TSX3510P     | DC Power Supply             | `TSX3510P`      |
| **Oxford Instruments** | ITC-503      | Temperature Controller      | `ITC503`        |
| [](<>)                 | IPS-120      | Magnet Controller           | `IPS120`        |
| [](<>)                 | ILM-200      | He Level Meter              | `ILM200`        |
| [](<>)                 | MercuryITC   | Temperature Controller      | `MercuryITC`    |
| **Lake Shore**         | 336          | Temperature Controller      | `LS336`         |
| **CryoCon**            | 22C          | Temperature Controller      | `CryoCon22C`    |
| **Stanford Research**  | SR830        | Lock-In Amplifier           | `SR830`         |
| [](<>)                 | SR560        | Voltage Pre-Amp             | `SR560`         |
| **Eurotherm**          | 2408         | Temperature Controller      | `ET2408`        |
| **Pico Technology**    | USB-TC08     | Thermometer (Multi-Channel) | `USBTC08`       |

## Prerequisites

Before being able to use JISA, you will need the Java Development Kit (JDK) 11 or newer installed.

### Linux

Either you'll already have OpenJDK installed or you simply need to run something like:

```
sudo apt install openjdk-11-jdk
```

### Windows and MacOS X

You can download pre-built OpenJDK packages (with installers) from the Adopt Open JDK website:

[https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot)

## Using JISA

You can use JISA in your project simply by including the JISA.jar file as a library. This will work so-long as your project uses Java 11 or newer.

<p align="center">

[![](https://img.shields.io/badge/Download-JISA.jar-teal)](https://github.com/OE-FET/JISA/raw/master/JISA.jar)

</p>

If using IntelliJ IDEA, this means adding JISA.jar to your project directory and, inside IDEA, right-clicking on it then selecting "Add as Library..."
