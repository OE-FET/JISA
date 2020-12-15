package jisa.addresses;

public class PipeAddress implements Address {
    
    private final String value;

    public PipeAddress(String value) {
        this.value = value.trim();
    }

    @Override
    public String toString() {
        return String.format("PIPE::%s::INSTR", value);
    }

    public String getPipeName() {
        return value;
    }

    public PipeAddress toPipeAddress() {
        return this;
    }

    public static class PipeParams extends AddressParams<PipeAddress> {

        public PipeParams() {

            addParam("Pipe Name", true);

        }

        @Override
        public PipeAddress createAddress() {
            return new PipeAddress(getString(0));
        }

        @Override
        public String getName() {
            return "Windows Named Pipe";
        }
    }
    
}
