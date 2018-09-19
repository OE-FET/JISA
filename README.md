# JISA - VISA-based experiment toolkit implemented in Java
Welcome to the JISA wiki! Very modern, wouldn't you say?

## What is JISA?
JISA is a Java libary that I originally designed because I really really really did not want to use LabView. It is largely comprised of three parts:
1. Instrument control via VISA
2. Dealing with experimental data
3. Creating simple GUIs to control and observe experiments

## Instrument Control
The guiding principle behind the instrument control is to provide a common interface for each type of device, with the code underneath bridging the gap between what the user sees and what the instrument itself requires.

As an example, despite Keithley 236 and 2450 SMUs being wildly different from each other in how they are remotely controlled, JISA presents them to the user in a standard manner, effectively "translating" standard function calls by the user into the different underlying command structure used by the different devices.

```Java
SMU smu1 = new K2450(new GPIBAddress(0, 15));
SMU smu2 = new K236(new GPIBAddress(0, 16));

smu1.setVoltage(5.0);
smu2.setVoltage(5.0);

smu1.turnOn();
smu2.turnOn();

double current1 = smu1.getCurrent();
double current2 = smu2.getCurrent();
```

In the example above, we have told both the K2450 and K236 to source 5 V and measure the resulting current. This is done using identical method calls on both despite the fact that the K236 and K2450 will implement these actions using fundamentally different approaches.

The same applies for all other types of device implemented so far, including lock-in amplifiers and DC power supplies.

## Handling Results
JISA provides a class called ``ResultList`` designed to encapsulate experimental data generated using the instrument control structures. This provides a quick way to record data and then export it as a CSV file, MATLAB script or even an ASCII table.

```Java
ResultList results = new ResultList("Voltage", "Current");
results.setUnits("V", "A");

SMU smu = new K2450(new SerialAddress(5));

smu.turnOn();

for (double v = 0; v <= 20; v += 2) {

    smu.setVoltage(v);
    results.addData(smu.getVoltage(), smu.getCurrent());

}

results.output("/path/to/file.csv");
results.outputMATLAB("/path/to/file.m", "V", "I");
results.outputTable();
```

```
+=============+=============+
| Voltage [V] | Current [A] |
+=============+=============+
| 0.000000    | 0.000000    |
+-------------+-------------+
| 2.000000    | 10.000000   |
+-------------+-------------+
| 4.000000    | 20.000000   |
+-------------+-------------+
| 6.000000    | 30.000000   |
+-------------+-------------+
| 8.000000    | 40.000000   |
+-------------+-------------+
| 10.000000   | 50.000000   |
+-------------+-------------+
| 12.000000   | 60.000000   |
+-------------+-------------+
| 14.000000   | 70.000000   |
+-------------+-------------+
| 16.000000   | 80.000000   |
+-------------+-------------+
| 18.000000   | 90.000000   |
+-------------+-------------+
| 20.000000   | 100.000000  |
+-------------+-------------+
```

## GUI Elements
JISA provides some basic GUI components, allowing you to piece them together to create a user-friendly front-end to control and/or observe your experiment. Most significantly including elements that will display the contents of a ``ResultList`` object in real-time to the user.

```Java
ResultList results = new ResultList("Voltage", "Current");
results.setUnits("V", "A");

// Creates a plot and tells it to watch our ResultList "results"
PlotWindow plot = PlotWindow.create("I-V plot", results);
plot.show();

SMU smu = new K2450(new SerialAddress(5));

smu.turnOn();

for (double v = 0; v <= 20; v += 2) {

    smu.setVoltage(v);
    results.addData(smu.getVoltage(), smu.getCurrent());

}
```
![Plot Window](https://i.imgur.com/PPgdyCa.png)

As mentioned, these GUI elements work in real-time, so every time a new data point gets added to ``results``, the plot will update (with a nifty animation too!)