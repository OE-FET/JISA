package JISA;

public class Main {

    public static void main(String[] args) {

        try {

            for (InstrumentAddress addr : VISA.getInstruments()) {
                System.out.println(addr.getVISAAddress());
            }

        } catch (VISAException e) {
            e.printStackTrace();
        }

    }

}
