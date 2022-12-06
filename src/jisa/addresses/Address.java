package jisa.addresses;

import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public interface Address {

    List<AddressInstantiator<?>> TYPES = new Reflections("jisa.addresses")
        .getSubTypesOf(Address.class)
        .stream()
        .filter(c -> !c.isInterface() && !Modifier.isAbstract(c.getModifiers()) && !c.equals(VISAAddress.class))
        .map(c -> {

            try {
                Constructor<? extends Address> constructor = c.getConstructor();
                return (AddressInstantiator<?>) constructor::newInstance;
            } catch (Exception e) {
                return (AddressInstantiator<?>) null;
            }

        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    interface AddressInstantiator<A extends Address> {
        A create() throws Exception;
    }

    /**
     * Attempts to create an Address object, of class representing its type, from a VISA/JISA address string.
     *
     * @param text Address string
     *
     * @return Address object representing address
     *
     * @throws InvalidAddressFormatException Upon address format not being recognised
     */
    static Address fromString(String text) throws InvalidAddressFormatException {

        for (AddressInstantiator<?> c : TYPES) {

            try {

                Address instance = c.create();
                instance.parseString(text);
                return instance;

            } catch (Exception ignored) {}

        }

        throw new InvalidAddressFormatException(text, "JISA-compatible");

    }

    /**
     * Attempts to create an Address object, of class representing its type, from a VISA/JISA address string, or failing
     * that simply wraps the address in as VISAAddress object.
     *
     * @param text Address string
     *
     * @return Address object
     */
    static Address parse(String text) {

        try {
            return fromString(text);
        } catch (InvalidAddressFormatException e) {
            return new VISAAddress(text);
        }

    }

    /**
     * Returns a textual representation of this address' type.
     *
     * @return Type of address
     */
    String getTypeName();

    /**
     * Returns the standard VISA text representation of this address.
     *
     * @return VISA text representation
     */
    String getVISAString();

    /**
     * Returns the JISA text representation of this address (may vary from the VISA representation in some cases).
     *
     * @return JISA text representation
     */
    default String getJISAString() {
        return getVISAString();
    }

    /**
     * Returns a map of this address' configurable parameters.
     *
     * @return Map of parameters
     */
    Map<String, Object> getParameters();

    /**
     * Set the parameters of this address from a given map.
     *
     * @param parameters Map of parameters to set
     */
    void setParameters(Map<String, Object> parameters);

    /**
     * Parses a text representation of this type of address, setting its parameters based on those in the text.
     *
     * @param text VISA/JISA text representation
     *
     * @throws InvalidAddressFormatException If the text representation is not formatted properly for this address type
     */
    void parseString(String text) throws InvalidAddressFormatException;

}
