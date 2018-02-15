package sample;

public enum Tables {
    departments(new String[]{"id", "name"}),
    employees(new String[]{"id", "first_name", "last_name", "pather_name", "position", "salary"}),
    departments_employees(new String[]{"id", "name", "first_name"}),
    projects(new String[]{"id", "name","cost", "date_beg", "date_end", "date_end_real"});

    private String[] namesColumn;

    Tables(String[] namesColumn) {
        this.namesColumn=namesColumn;
    }

    public String[] getNamesColumn() {
        return namesColumn;
    }
    public String getNameColumn_i(int i) {
        return namesColumn[i];
    }
}
