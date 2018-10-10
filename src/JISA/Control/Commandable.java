package JISA.Control;

import java.util.HashMap;

public interface Commandable {

    DeviceCommand[] getCommands();

    DeviceCommand getCommand(int index);

    String getName();

    HashMap<String, Class> getNameableParams();

}
