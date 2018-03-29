package JPIB;

import java.io.IOException;

public interface ICondition {

    public boolean isMet(int i) throws IOException, DeviceException;

}
