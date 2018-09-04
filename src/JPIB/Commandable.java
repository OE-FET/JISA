package JPIB;

import java.util.List;

public interface Commandable {

    public DeviceCommand[] getCommands();

    public DeviceCommand getCommand(int index);

    public String getName();

}
