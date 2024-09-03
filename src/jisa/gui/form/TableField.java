package jisa.gui.form;

import jisa.results.Column;
import jisa.results.ResultTable;

import java.util.List;

public interface TableField extends Field<ResultTable> {

    void setColumns(Column... columns);

    List<Column> getColumns();

}
