package sample;

import javafx.beans.property.StringProperty;

public class TableBD {
    private String name;
    private StringProperty[] namesColumns;

    public TableBD(StringProperty[] namesColumns, String name) {
        this.name = name;
        this.namesColumns = namesColumns;
    }

    public StringProperty NameProperty(int i) {
        return namesColumns[i];
    }
    public StringProperty getValueId() {
        return namesColumns[0];
    }
    public StringProperty getValue(int i) {
        return namesColumns[i];
    }

    @Override
    public String toString() {
        return String.valueOf(print_valuesColums());
    }

    private StringBuilder print_valuesColums() {
        StringBuilder stringBuilder = new StringBuilder();
        for (StringProperty s:namesColumns)
            stringBuilder.append(String.format("%-20s",s.getValue()));
        return stringBuilder;
    }


}
