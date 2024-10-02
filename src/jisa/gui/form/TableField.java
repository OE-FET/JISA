package jisa.gui.form;

import jisa.results.Column;
import jisa.results.DataTable;

import java.util.List;

public interface TableField extends Field<DataTable> {

    void setColumns(Column... columns);

    List<Column> getColumns();

}
