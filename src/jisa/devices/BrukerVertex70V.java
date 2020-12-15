package jisa.devices;

import jisa.addresses.Address;
import jisa.experiment.Spectrum;
import jisa.maths.Range;
import jisa.visa.PipeDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BrukerVertex70V extends PipeDevice implements Spectrometer {

    private double integrationTime = 100e-3;
    private double minFrequency = 100e-9;
    private double maxFrequency = 900e-9;

    public BrukerVertex70V(Address address) throws IOException {

        super(address);

    }

    @Override
    public String getIDN() {
        return "Bruker Vertex 70v FT-IR Spectrometer";
    }


    @Override
    public double getIntegrationTime() throws IOException, DeviceException {
        return integrationTime;
    }

    @Override
    public void setIntegrationTime(double time) throws IOException, DeviceException {
        integrationTime = time;
    }

    @Override
    public double getMinFrequency() throws IOException, DeviceException {
        return minFrequency;
    }

    @Override
    public void setMinFrequency(double min) throws IOException, DeviceException {
        minFrequency = min;
    }

    @Override
    public double getMaxFrequency() throws IOException, DeviceException {
        return maxFrequency;
    }

    @Override
    public void setMaxFrequency(double max) throws IOException, DeviceException {
        maxFrequency = max;
    }

    @Override
    public Spectrum measureSpectrum() throws IOException, DeviceException {

        write("MEASURE_SAMPLE sample.xpm");

        String status = read();

        if (!status.equals("OK")) {
            throw new DeviceException("Error on Bruker Spectrometer.");
        }

        int                      numRows        = readInt();
        double                   firstFrequency = readDouble();
        double                   lastFrequency  = readDouble();
        double                   scale          = readDouble();
        double[]                 frequencies    = Range.linear(firstFrequency, lastFrequency, numRows).doubleArray();
        List<Spectrum.DataPoint> dataPoints     = new ArrayList<>(numRows);

        for (int i = 0; i < numRows; i++) {
            dataPoints.add(new Spectrum.DataPoint(frequencies[i], scale * readDouble()));
        }

        return new Spectrum(dataPoints);

    }

}
