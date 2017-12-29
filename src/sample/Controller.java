package sample;

import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.postgresql.util.PSQLException;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Controller {

    public Pane panel1;
    public Button login;
    public TextField userTF;
    public TextField passwordTF;

    public Pane panel2;
    public ChoiceBox choiceBoxTable;
    public ChoiceBox choiceBoxTableJ;
    public TableView table;
    public Button addInTable;
    public Button updateInTable;
    public Button deleteInTable;
    public Button printTable;

    private String url = Main.connectionParameters[1];
    private String user = Main.connectionParameters[2];
    private String password = Main.connectionParameters[3];
    private String selectedNameTable;
    private int selectedNumberColumns;
    private Stage stageAdd;
    private List<TableBD> rowsOnTable;
    private List<TextField> textFieldsForChangeInTable;
    final private double widthElement = 250;
    private int access;
    private String amount;

    public void onLogin() {

        if (checkUsers())
            fillChoiceTable();
    }
    private boolean checkUsers() {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {

            ResultSet res = statement.executeQuery("select mod from users where name = \'" + userTF.getText() + "\' and " +
                    "password = md5(\'" + passwordTF.getText() + "\');");
            if (res.next()) {
                access = res.getInt("mod");
                panel1.setVisible(false);
                panel2.setVisible(true);
                return true;
            } else showErrorAlert("Не удалось подключиться", "Возможно, ошибка при вводе логина\\пароля");
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Не удалось подключиться", "Возможно, ошибка в файле подключения");
        }
        return false;
    }
    private void fillChoiceTable() {
        try {
            //заполняем компоненты
//            choiceBoxTable.setItems(FXCollections.observableArrayList(Tables.values()));

            choiceBoxTable.setItems(FXCollections.observableArrayList(Tables.departments, Tables.employees));
            choiceBoxTable.setPrefWidth(widthElement);
            //получаем ВЫБОР таблицы с помощью слушателя (и выводим выбор)
            choiceBoxTable.getSelectionModel().selectedIndexProperty().addListener(
                    (ObservableValue<? extends Number> ol, Number old_val, Number new_val) -> {
//                        getRowsBySelectedTable.setText("Показать таблицу ");
                        selectedNameTable = String.valueOf(choiceBoxTable.getItems().get(new_val.intValue()));
                        selectedNumberColumns = Tables.valueOf(selectedNameTable).getNamesColumn().length;
                        System.out.println(choiceBoxTable.getItems().get(new_val.intValue()));
//                        getRowsBySelectedTable.setText(getRowsBySelectedTable.getText()+selectedNameTable);
                        getRowsBySelectedTable();
//                        choiceBoxTableJ.getItems().clear();
                    });

            choiceBoxTableJ.setItems(FXCollections.observableArrayList(Tables.departments_employees, Tables.projects));
            choiceBoxTableJ.setPrefWidth(widthElement);
            //получаем ВЫБОР таблицы с помощью слушателя (и выводим выбор)
            choiceBoxTableJ.getSelectionModel().selectedIndexProperty().addListener(
                    (ObservableValue<? extends Number> ol, Number old_val, Number new_val) -> {
//                        getRowsBySelectedTable.setText("Показать таблицу ");
                        selectedNameTable = String.valueOf(choiceBoxTableJ.getItems().get(new_val.intValue()));
                        selectedNumberColumns = Tables.valueOf(selectedNameTable).getNamesColumn().length;
                        System.out.println(choiceBoxTableJ.getItems().get(new_val.intValue()));
//                        getRowsBySelectedTable.setText(getRowsBySelectedTable.getText()+selectedNameTable);
                        getRowsBySelectedTable();
                    });


        } catch (ArrayIndexOutOfBoundsException e) {

        }
    }

    public void onBackToLogin() {
        selectedNameTable = null;

        choiceBoxTable.getItems().removeAll();
//        choiceBoxTable = null;

        table.getItems().clear();//возможно не нужно
        table.getColumns().clear();
//        table = null;
        table.setVisible(false);

        setButtonVisible(false);
        printTable.setVisible(false);
        panel1.setVisible(true);
        panel2.setVisible(false);
    }
    private void setButtonVisible(boolean value) {
        addInTable.setVisible(value);
        updateInTable.setVisible(value);
        deleteInTable.setVisible(value);

    }
    private void showErrorAlert(String error, String help) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Внимание...");
        alert.setHeaderText(error);
        alert.setContentText(help);
        //СТИЛЬ ОФОРМЛЕНИЯ ДЛЯ Alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #A52A2A; " +
                "-fx-font-size: 14pt; " +
                "-fx-text: white;" +
                "");
        alert.showAndWait();
    }

    private void getRowsBySelectedTable() {
        String nameTable = selectedNameTable;
        String[] namesColumns = Tables.valueOf(selectedNameTable).getNamesColumn();

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            //Для использования SQL запросов существуют 3 типа объектов:
            //1.Statement: используется для простых случаев без параметров

            //resultSet это указатель на первую строку с выборки
            //чтобы вывести данные мы будем использовать
            //метод next() , с помощью которого переходим к следующему элементу

            if (selectedNameTable.equals("departments_employees")) {
                ResultSet resultSet = statement.executeQuery("select departments_employees.id,departments.name,employees.first_name from  departments_employees join departments on department_id = departments.id join employees on employee_id = employees.id");
//                                                               select departments_employees.id,departments.name,employees.first_name from  departments_employees join departments on department_id = departments.id join employees on employee_id = employees.id
                rowsOnTable = new ArrayList<>();
                while (resultSet.next()) {
                    rowsOnTable.add(new TableBD(getResultSetForNamesColumns(namesColumns, resultSet), nameTable));
                }
                //TEST вывод таблицы в консоль
                for (TableBD t : rowsOnTable) {
                    System.out.printf("%s\n", t);
                }
                try {
                    fillTableView(namesColumns, rowsOnTable);
                } catch (NullPointerException e) {
                    System.out.println("НЕТ ЗНАЧЕНИЙ" + "  ОШИБКА обработана");
                }

            }else if (selectedNameTable.equals("projects")) {
                ResultSet resultSet = statement.executeQuery("select projects.id,projects.cost,projects.date_beg,projects.date_end,projects.date_end_real,departments.name from projects join departments on department_id = departments.id ");
//                                                               select departments_employees.id,departments.name,employees.first_name from  departments_employees join departments on department_id = departments.id join employees on employee_id = employees.id
//                                                                         projects(new String[]{"id", "cost", "date_beg", "date_end", "date_end_real", "name"})

                rowsOnTable = new ArrayList<>();
                while (resultSet.next()) {


                    rowsOnTable.add(new TableBD(getResultSetForNamesColumns(namesColumns, resultSet), nameTable));
                }
                //TEST вывод таблицы в консоль
                for (TableBD t : rowsOnTable) {
                    System.out.printf("%s\n", t);
                }
                try {
                    fillTableView(namesColumns, rowsOnTable);
                } catch (NullPointerException e) {
                    System.out.println("НЕТ ЗНАЧЕНИЙ" + "  ОШИБКА обработана");
                }
        }else{
            ResultSet resultSet = statement.executeQuery("select * from " + nameTable);

            rowsOnTable = new ArrayList<>();
            while (resultSet.next()) {
                rowsOnTable.add(new TableBD(getResultSetForNamesColumns(namesColumns, resultSet), nameTable));
            }
            //TEST вывод таблицы в консоль
            for (TableBD t : rowsOnTable) {
                System.out.printf("%s\n", t);
            }
            try {
                fillTableView(namesColumns, rowsOnTable);
            } catch (NullPointerException e) {
                System.out.println("НЕТ ЗНАЧЕНИЙ" + "  ОШИБКА обработана");
            }
        }
//                }
        } catch (
                SQLException e) {
            e.printStackTrace();
        }
    }
    private StringProperty[] getResultSetForNamesColumns(String[] namesColumns, ResultSet resultSet) throws
            SQLException {
        StringProperty[] str = new javafx.beans.property.StringProperty[namesColumns.length];
        for (int i = 0; i < namesColumns.length; i++) {
            str[i] = new SimpleStringProperty(resultSet.getString(namesColumns[i]));
        }
        return str;
    }
    private void fillTableView(String[] namesColumn, List rowsOnTable) {
        //удаляем (отчищаем) TableView
        table.getItems().clear();//возможно не нужно
        table.getColumns().clear();

        //отображение данных строк
        ObservableList<TableBD> observableList = FXCollections.observableArrayList();
        observableList.addAll(rowsOnTable);
        table.setItems(observableList);

        //делаем столбцы
        for (int i = 0; i < namesColumn.length; i++) {
            TableColumn<TableBD, String> tableColumn = new TableColumn(namesColumn[i]);
            int finalI = i;
            // Инициализация таблицы адресатов
            // Метод setCellValueFactory(...) определяет, какое поле внутри класса TableBD будут использоваться для конкретного столбца в таблице
            if (i == 0) {
                tableColumn.setCellValueFactory(cellData -> cellData.getValue().NameProperty(finalI));
            } else
                tableColumn.setCellValueFactory(cellData -> cellData.getValue().NameProperty(finalI));

            //добавить в TableView стлобец
            table.getColumns().add(tableColumn);
        }

        table.setVisible(true);
        if (access == 1) {
            setButtonVisible(true);
            printTable.setVisible(true);
        } else {
            setButtonVisible(false);
            printTable.setVisible(true);
        }
    }

    public void onAdd() {
        System.out.println("PRESS ADD");
        String title = "Добавление в " + selectedNameTable;
        changeInTable(title, ChangeTableInDB.INSERT);
    }
    public void onUpdate() {
        System.out.println("PRESS CREATE");
        String title = "Изменение в " + selectedNameTable;
        changeInTable(title, ChangeTableInDB.UPDATE);
    }
    public void onDelete() {
        System.out.println("PRESS DELETE");
        String title = "Удаление в " + selectedNameTable;
        changeInTable(title, ChangeTableInDB.DELETE);
    }
    public void onPrint() throws IOException {
        System.out.println("PRESS PRINT");

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить");
//            chooser.setInitialFileName(ReportList.reports.get(ReportTable.currentInd).name);

        chooser.setInitialDirectory(
                new File(System.getProperty("user.home")));

        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Все файлы", "*.*"));

        File file = chooser.showSaveDialog(stageAdd);
        if (file != null) {
//                int row_num = main.reportTableView.getItems().size();
            int row_num = table.getItems().size();
            int col_num = selectedNumberColumns;
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(file.getAbsolutePath()));
            Document doc = new Document(pdfDoc);

            PdfFontFactory.register("FreeSans.ttf", "FreeSans");
            PdfFont font = PdfFontFactory.createRegisteredFont("FreeSans", "Cp1251");
            Paragraph p = new Paragraph();
            p.setFont(font);
            p.setFontSize(15f);
            p.setTextAlignment(TextAlignment.CENTER);
            p.add(selectedNameTable);
            doc.add(p);
            Table table1 = new Table(col_num);
            for (int i = 0; i < col_num; i++) {
                Cell cell = new Cell();
                cell.setFont(font);
                cell.setBorder(new SolidBorder(Color.BLACK, 2));
                cell.add(Tables.valueOf(selectedNameTable).getNameColumn_i(i));
                table1.addCell(cell);
            }
            for (int i = 0; i < row_num; i++) {
//                    ObservableList rowList = (ObservableList) main.reportTableView.getItems().get(i);
                TableBD tableBD = (TableBD) table.getItems().get(i);
                for (int j = 0; j < col_num; j++) {
                    Cell cell = new Cell();
                    cell.setFont(font);

//                        tbd.getValueId().getValue()
                    cell.add(tableBD.getValue(j).getValue());
                    table1.addCell(cell);
                }
            }
            doc.add(table1);
            doc.close();
        }
    }

    private void changeInTable(String title, String choice) {
        if (choice.equals(ChangeTableInDB.INSERT)) {
            VBox vbox = createVBox();
            placeElementsOnVBoxFromTable(vbox, 0, null, choice);
            createStage(title, vbox);
        }
        if (choice.equals(ChangeTableInDB.UPDATE) || choice.equals(ChangeTableInDB.DELETE))
            try {
                TableBD tbd = (TableBD) table.getSelectionModel().getSelectedItem();
                //test
                System.out.println(tbd.getValueId().getValue());
                int id = Integer.parseInt(tbd.getValueId().getValue());

                VBox vbox = createVBox();
                placeElementsOnVBoxFromTable(vbox, id, tbd, choice);
                createStage(title, vbox);

            } catch (NullPointerException e) {
                showErrorAlert("Ошибка", "Вы не выбрали строку");
            }
    }
    private VBox createVBox() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10, 20, 10, 10));
        vbox.setSpacing(5);
        vbox.setStyle("-fx-background-color: #800000;");
        vbox.getStylesheets().add(Controller.class.getResource("DarkTheme.css").toExternalForm());
        return vbox;
    }
    private void placeElementsOnVBoxFromTable(VBox vbox, int id, TableBD tbd, String choice) {
        textFieldsForChangeInTable = new ArrayList<>();

        for (int i = 0; i < selectedNumberColumns; i++) {
            Label label = new Label(Tables.valueOf(selectedNameTable).getNameColumn_i(i));
            TextField textField = new TextField();

            if (i == 0) {
                if (choice.equals(ChangeTableInDB.INSERT))
                    textField.setPromptText("AUTO");
                else
                    textField.setText(String.valueOf(id));
                textField.setDisable(true);
                vbox.getChildren().addAll(label, textField);
            } else {
                if (choice.equals(ChangeTableInDB.INSERT)) {
                    textFieldsForChangeInTable.add(textField);
                    vbox.getChildren().addAll(label, textField);
                }

                if (choice.equals(ChangeTableInDB.UPDATE)) {
                    textField.setText(String.valueOf(tbd.getValue(i).getValue()));
                    textFieldsForChangeInTable.add(textField);
                    vbox.getChildren().addAll(label, textField);
                }
                if (choice.equals(ChangeTableInDB.DELETE)) {
                    textField.setText(String.valueOf(tbd.getValue(i).getValue()));
                    textField.setDisable(true);
                    textFieldsForChangeInTable.add(textField);
                    vbox.getChildren().addAll(label, textField);
                }
            }
        }
        Button buttonOK = new Button("Ok");
        buttonOK.setPrefWidth(150);

        buttonOK.setOnAction(event -> {
            System.out.println("PRESS OK from AddInTable");
            if (checkTheFilling(textFieldsForChangeInTable)) {
                String newData = String.valueOf(getTextFromTextFields(textFieldsForChangeInTable, choice));
                executeSqlQuery(id, newData, choice);
                getRowsBySelectedTable();
                stageAdd.close();
            } else showErrorAlert("Ошибка:", "Заполните все поля");
        });

        Button buttonCancel = new Button("Cancel");
        buttonCancel.setPrefWidth(150);
        buttonCancel.setOnAction(event -> stageAdd.close());
//        buttonOK.setCenterShape(true);
        vbox.getChildren().addAll(buttonOK, buttonCancel);
    }
    private void createStage(String title, VBox vbox) {
        stageAdd = new Stage();
        stageAdd.setTitle(title);
        stageAdd.setScene(new Scene(vbox, 500, 550));
        stageAdd.initOwner(Main.stage1);
        stageAdd.initModality(Modality.APPLICATION_MODAL);
        stageAdd.showAndWait();
    }
    private boolean checkTheFilling(List<TextField> textFields) {
        boolean chech = true;

        for (TextField tf : textFields) {
            System.out.println(tf.getText());
            if (tf.getText().equals("")) {
                chech = false;
                continue;
            }
        }
        return chech;
    }
    private StringBuilder getTextFromTextFields(List<TextField> textFields, String choice) {
        StringBuilder data = new StringBuilder();

        if (choice.equals(ChangeTableInDB.INSERT)) {
            for (TextField tf : textFields)
                data.append("\'" + tf.getText() + "\'" + ",");
        }
        if (!choice.equals(ChangeTableInDB.INSERT)) {
            for (int i = 1; i < selectedNumberColumns; i++) {
                data.append(Tables.valueOf(selectedNameTable).getNameColumn_i(i) + " = " + "\'" + textFields.get(i - 1).getText() + "\', ");
            }
        }
        data.deleteCharAt(data.lastIndexOf(","));
        return data;
    }
    private void executeSqlQuery(int id, String newDataForUpdateOrInsert, String choice) {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {

            switch (choice) {
                case ChangeTableInDB.UPDATE:
                    statement.executeUpdate("update " + selectedNameTable + " SET " + newDataForUpdateOrInsert + " where id = " + id);
                    break;
                case ChangeTableInDB.INSERT:
                    String stringOptionsForTable = String.valueOf(createStringOptionsForTable(selectedNameTable));
                    statement.executeUpdate("insert into " + stringOptionsForTable + " values (" + newDataForUpdateOrInsert + ")");
                    break;
                case ChangeTableInDB.DELETE:
                    statement.executeUpdate("delete from " + selectedNameTable + " where id = " + id);
                    break;
            }
        } catch (PSQLException e) {
            showErrorAlert("Ошибка: ", String.valueOf(e.getServerErrorMessage()));
            System.out.println(e.getServerErrorMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Ошибка: ", "Проверьте введенные данные");
        }
    }
    private StringBuilder createStringOptionsForTable(String selectedNameTable) {
        StringBuilder optionsForTable = new StringBuilder();
        optionsForTable.append(selectedNameTable + "(");

        for (int i = 1; i < selectedNumberColumns; i++) {
            optionsForTable.append(Tables.valueOf(selectedNameTable).getNameColumn_i(i) + ", ");
        }

        optionsForTable.append(")");
        optionsForTable.deleteCharAt(optionsForTable.lastIndexOf(","));

        return optionsForTable;
    }

    public void onReport() {
        System.out.println("PRESS REPORT");
        VBox vBox = createVBox();

        Label l1 = new Label("                             КУРСОР");
        Label l2 = new Label("Задайте время, с которого будем считать доход ");
        Label l3 = new Label(" формат (YYYY-mm-dd)");
        TextField textField = new TextField();

        Label l4 = new Label("Прибыли от завершенных проектов от");
        l4.setVisible(false);
        TextField textField1 = new TextField();
        textField1.setVisible(false);
        textField1.setDisable(true);

        Button buttonOK = new Button("Вывести");
        buttonOK.setPrefWidth(150);

        buttonOK.setOnAction(event -> {
            System.out.println("PRESS OK from Report");
            if (checkInData(textField.getText())) {
                System.out.println("RITE");
                getAmount(textField.getText());
                l4.setText("Прибыли от завершенных проектов от " + textField.getText());
                l4.setVisible(true);
                textField1.setVisible(true);
                textField1.setText(amount + " руб");

            }
        });

        Button buttonCancel = new Button("Назад");
        buttonCancel.setPrefWidth(150);
        buttonCancel.setOnAction(event -> {
            stageAdd.close();
            l4.setVisible(false);
            textField1.setVisible(false);
        });
        textField.setText(amount);
        vBox.getChildren().addAll(l1, l2, l3, textField, buttonOK, buttonCancel, l4, textField1);
        createStage("Отчет", vBox);
    }
    private void getAmount(String text) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            //Вызываем функцию myFunc (хранится в БД)
            CallableStatement callableStatement = connection.prepareCall(
                    "select * from getsummprib(to_date(?,'Y-m-d'))");
            //Задаём входные параметры
            callableStatement.setString(1, text);
            //Выполняем запрос
            ResultSet result3 = callableStatement.executeQuery();
            result3.next();
            System.out.println(result3.getString("amount"));
            amount = result3.getString("amount");

        } catch (PSQLException e) {
            showErrorAlert("Ошибка: ", String.valueOf(e.getServerErrorMessage()));
            System.out.println(e.getServerErrorMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private boolean checkInData(String text) {
        try {
            Date date = Date.valueOf(text);
            return true;
        } catch (Exception e) {
            showErrorAlert("Ошибка", "Некорректный формат даты");
            return false;
        }
    }
}