package jisa.gui.form;

import java.util.List;

public interface ChoiceField<T> extends Field<T> {

    void setChoices(String... options);

    List<String> getChoices();

}
