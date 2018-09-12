package JISA.Control;

import java.util.HashMap;

public interface Commandable {

    public DeviceCommand[] getCommands();

    public DeviceCommand getCommand(int index);

    public String getName();

    public HashMap<String, Class> getNameableParams();

}
