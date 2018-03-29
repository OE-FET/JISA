package JPIB;

import java.io.IOException;

public interface DoubleReturn {

    public double getValue() throws IOException, DeviceException;

}
