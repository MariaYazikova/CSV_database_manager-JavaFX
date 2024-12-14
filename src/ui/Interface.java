package ui;

import database.Database;
import database.Record;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextField;
import javafx.geometry.Insets;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

public class Interface extends Application {
    private Database db;
    private Label infoLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Меню");
        infoLabel = new Label();
        createDatabase();

        //кнопки
        Button btnCreateDB = new Button("Создать");
        Button btnOpenDB = new Button("Открыть");
        Button btnDeleteDB = new Button("Удалить");
        Button btnClearDB = new Button("Очистить");
        Button btnAddRecord = new Button("Добавить запись");
        Button btnSearchRecords = new Button("Найти запись");
        Button btnDeleteRecords = new Button("Удалить запись");
        Button btnEditRecord = new Button("Редактировать запись");
        Button btnBackupDB = new Button("Создать backup-файл");
        Button btnRestoreDB = new Button("Восстановить backup-файл");

        //обработка нажатий
        btnCreateDB.setOnAction(e -> createDatabase());
        btnOpenDB.setOnAction(e -> openDatabase());
        btnDeleteDB.setOnAction(e -> deleteDatabase());
        btnClearDB.setOnAction(e -> clearDatabase());
        btnAddRecord.setOnAction(e -> openAddRecordWindow());
        btnSearchRecords.setOnAction(e -> openSearchRecordsWindow());
        btnDeleteRecords.setOnAction(e -> openDeleteRecordsWindow());
        btnEditRecord.setOnAction(e -> openEditRecordWindow());
        btnBackupDB.setOnAction(e -> backupDatabase());
        btnRestoreDB.setOnAction(e -> restoreDatabase());

        HBox hbox1 = new HBox(10, btnCreateDB, btnOpenDB);
        hbox1.setAlignment(Pos.CENTER);
        HBox hbox2 = new HBox(10, btnDeleteDB, btnClearDB);
        hbox2.setAlignment(Pos.CENTER);
        HBox hbox3 = new HBox(10, btnAddRecord, btnSearchRecords);
        hbox3.setAlignment(Pos.CENTER);
        HBox hbox4 = new HBox(10, btnDeleteRecords, btnEditRecord);
        hbox4.setAlignment(Pos.CENTER);
        HBox hbox5 = new HBox(10, btnBackupDB);
        hbox5.setAlignment(Pos.CENTER);
        HBox hbox6 = new HBox(10, btnRestoreDB);
        hbox6.setAlignment(Pos.CENTER);

        VBox mainVBox = new VBox(30, new VBox(10, hbox1, hbox2), new VBox(10, hbox3, hbox4), new VBox(10, hbox5, hbox6), infoLabel);
        mainVBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(mainVBox, 300, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //создать
    private void createDatabase() {
        try {
            db = new Database("database.csv");
            String message = db.getMessage();
            Platform.runLater(() -> infoLabel.setText(message));
        } catch (Exception e) {
            Platform.runLater(() -> infoLabel.setText("Не удалось создать базу данных: " + e.getMessage()));
        }
    }

    //открыть
    private void openDatabase() {
        try {
            if (db == null) {
                Platform.runLater(() -> infoLabel.setText("База данных не создана"));
                return;
            }
            List<String> dataContents = db.displayDatabaseContents();
            if (dataContents == null) {
                Platform.runLater(() -> infoLabel.setText("База данных не создана"));
                return;
            }
            Stage newWindow = new Stage();
            newWindow.setTitle("База данных");
            TableView<Record> tableView = new TableView<>();
            tableView.setItems(javafx.collections.FXCollections.observableArrayList(parseData(dataContents)));


            TableColumn<Record, Integer> idColumn = new TableColumn<>("ID");
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

            TableColumn<Record, String> nameColumn = new TableColumn<>("Name");
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

            TableColumn<Record, Double> salaryColumn = new TableColumn<>("Salary");
            salaryColumn.setCellValueFactory(new PropertyValueFactory<>("salary"));

            TableColumn<Record, Boolean> needsRaiseColumn = new TableColumn<>("Needs Raise");
            needsRaiseColumn.setCellValueFactory(new PropertyValueFactory<>("needsRaise"));

            tableView.getColumns().addAll(idColumn, nameColumn, salaryColumn, needsRaiseColumn);

            VBox vbox = new VBox(tableView);
            Scene scene = new Scene(vbox, 600, 400);
            newWindow.setScene(scene);
            newWindow.show();
        } catch (Exception e) {
            System.err.println("Не удалось открыть базу данных: " + e.getMessage());
        }
    }

    //вспомогательная для открытия
    private List<Record> parseData(List<String> dataContents) {
        List<Record> records = new ArrayList<>();
        boolean firstLine = true;
        for (String line : dataContents) {
            if (firstLine) {
                firstLine = false;
                continue;
            }
            String[] fields = line.split(",");
            try {
                int id = Integer.parseInt(fields[0].trim());
                String name = fields[1].trim();
                double salary = Double.parseDouble(fields[2].trim());
                boolean needsRaise = Boolean.parseBoolean(fields[3].trim());
                records.add(new Record(id, name, salary, needsRaise));
            } catch (NumberFormatException e) {
                System.err.println("Неправильный формат в строке: " + line);
            }
        }
        return records;
    }

    //удалить
    private void deleteDatabase() {
        if (db == null) {
            Platform.runLater(() -> infoLabel.setText("База данных не создана"));
            return;
        }
        db.deleteDatabase();
        String message = db.getMessage();
        Platform.runLater(() -> infoLabel.setText(message));
    }

    //очистить
    private void clearDatabase() {
        if (db == null) {
            Platform.runLater(() -> infoLabel.setText("База данных не создана"));
            return;
        }
        db.clearDatabase();
        String message = db.getMessage();
        Platform.runLater(() -> infoLabel.setText(message));
    }

    //добавить запись
    private void openAddRecordWindow() {
        if (db == null || !db.getDbFilePath().exists()) {
            infoLabel.setText("База данных не создана");
            return;
        }
        Stage addRecordWindow = new Stage();
        addRecordWindow.setTitle("Добавление записи");
        Label idLabel = new Label("ID:");
        TextField idField = new TextField();

        Label nameLabel = new Label("Имя:");
        TextField nameField = new TextField();

        Label salaryLabel = new Label("Зарплата:");
        TextField salaryField = new TextField();

        Label needsRaiseLabel = new Label("Нуждается в повышении (true/false):");
        TextField needsRaiseField = new TextField();

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button addButton = new Button("Добавить");
        addButton.setOnAction(event -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String name = nameField.getText().trim();
                double salary = Double.parseDouble(salaryField.getText().trim());
                boolean needsRaise = Boolean.parseBoolean(needsRaiseField.getText().trim());
                Record record = new Record(id, name, salary, needsRaise);
                db.addRecord(record);
                addRecordWindow.close();
            } catch(IllegalArgumentException e){
                errorLabel.setText(e.getMessage());
            }
            catch (Exception e) {
                System.err.println("Не удалось добавить запись: " + e.getMessage());
            }
        });
        VBox layout = new VBox(10, idLabel, idField, nameLabel, nameField, salaryLabel, salaryField, needsRaiseLabel, needsRaiseField, errorLabel, addButton);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 300, 400);
        addRecordWindow.setScene(scene);
        addRecordWindow.show();
    }


    //найти запись
    public void openSearchRecordsWindow() {
        if (db == null || !db.getDbFilePath().exists()) {
            infoLabel.setText("База данных не создана");
            return;
        }
        Stage searchWindow = new Stage();
        searchWindow.setTitle("Поиск записей");

        ComboBox<String> searchCriteriaComboBox = new ComboBox<>();
        searchCriteriaComboBox.getItems().addAll("ID", "Name", "Salary", "Needs Raise");
        searchCriteriaComboBox.setValue("ID");

        TextField searchField = new TextField();

        Button searchButton = new Button("Поиск");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        TableView<Record> resultTable = new TableView<>();
        TableColumn<Record, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Record, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Record, Double> salaryColumn = new TableColumn<>("Salary");
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("salary"));

        TableColumn<Record, Boolean> needsRaiseColumn = new TableColumn<>("Needs Raise");
        needsRaiseColumn.setCellValueFactory(new PropertyValueFactory<>("needsRaise"));

        resultTable.getColumns().addAll(idColumn, nameColumn, salaryColumn, needsRaiseColumn);
        searchButton.setOnAction(e -> {
            try {
                List<Record> results = null;
                String criteria = searchCriteriaComboBox.getValue();
                String searchValue = searchField.getText().trim();

                if (criteria.equals("ID")) {
                    int id = Integer.parseInt(searchValue);
                    results = List.of(db.searchById(id));
                } else if (criteria.equals("Name")) {
                    results = db.searchByName(searchValue);
                } else if (criteria.equals("Salary")) {
                    double salary = Double.parseDouble(searchValue);
                    results = db.searchBySalary(salary);
                } else if (criteria.equals("Needs Raise")) {
                    boolean needsRaise = Boolean.parseBoolean(searchValue);
                    results = db.searchByNeedsRaise(needsRaise);
                }
                ObservableList<Record> data = FXCollections.observableArrayList(results);
                resultTable.setItems(data);
                errorLabel.setText(" ");

            }
            catch (Exception ex) {
                errorLabel.setText("Совпадения не найдены");
            }
        });
        VBox layout = new VBox(10, searchCriteriaComboBox, searchField, searchButton, resultTable, errorLabel);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 600, 400);
        searchWindow.setScene(scene);
        searchWindow.show();
    }

    //удалить запись
    private void openDeleteRecordsWindow() {
        if (db == null || !db.getDbFilePath().exists()) {
            infoLabel.setText("База данных не создана");
            return;
        }
        Stage deleteWindow = new Stage();
        deleteWindow.setTitle("Удаление записей");
        ComboBox<String> deleteCriteriaComboBox = new ComboBox<>();
        deleteCriteriaComboBox.getItems().addAll("ID", "Name", "Salary", "Needs Raise");
        deleteCriteriaComboBox.setValue("ID");

        TextField deleteField = new TextField();

        Button deleteButton = new Button("Удалить");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        deleteButton.setOnAction(e -> {
            try {
                String criteria = deleteCriteriaComboBox.getValue();
                String deleteValue = deleteField.getText().trim();

                if (criteria.equals("ID")) {
                    int id = Integer.parseInt(deleteValue);
                    db.deleteRecord(id);
                } else if (criteria.equals("Name")) {
                    db.deleteByName(deleteValue);
                } else if (criteria.equals("Salary")) {
                    double salary = Double.parseDouble(deleteValue);
                    db.deleteBySalary(salary);
                } else if (criteria.equals("Needs Raise")) {
                    boolean needsRaise = Boolean.parseBoolean(deleteValue);
                    db.deleteByNeedsRaise(needsRaise);
                }
                infoLabel.setText("Записи успешно удалены");
                deleteWindow.close();
            } catch (Exception ex) {
                errorLabel.setText("Совпадения не найдены");
            }
        });
        VBox layout = new VBox(10, deleteCriteriaComboBox, deleteField, deleteButton, errorLabel);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 300, 200);
        deleteWindow.setScene(scene);
        deleteWindow.show();
    }

    //редактировать запись
    private void openEditRecordWindow() {
        if (db == null || !db.getDbFilePath().exists()) {
            infoLabel.setText("База данных не создана");
            return;
        }
        Stage editRecordWindow = new Stage();
        editRecordWindow.setTitle("Редактирование записи");

        Label idLabel = new Label("ID записи для редактирования:");
        TextField idField = new TextField();

        Label newNameLabel = new Label("Новое имя:");
        TextField newNameField = new TextField();

        Label newSalaryLabel = new Label("Новая зарплата:");
        TextField newSalaryField = new TextField();

        Label newNeedsRaiseLabel = new Label("Нуждается в повышении (true/false):");
        TextField newNeedsRaiseField = new TextField();

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button editButton = new Button("Редактировать");
        editButton.setOnAction(event -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String newName = newNameField.getText().trim();
                double newSalary = Double.parseDouble(newSalaryField.getText().trim());
                boolean newNeedsRaise = Boolean.parseBoolean(newNeedsRaiseField.getText().trim());
                db.editRecord(id, newName, newSalary, newNeedsRaise);
                infoLabel.setText("Запись успешно отредактирована");
                editRecordWindow.close();
            } catch (IllegalArgumentException e) {
                errorLabel.setText(e.getMessage());
            } catch (Exception e) {
                System.err.println("Ошибка при редактировании записи: " + e.getMessage());
                errorLabel.setText("Ошибка при редактировании записи");
            }
        });
        VBox layout = new VBox(10, idLabel, idField, newNameLabel, newNameField, newSalaryLabel, newSalaryField, newNeedsRaiseLabel, newNeedsRaiseField, errorLabel, editButton);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 300, 400);
        editRecordWindow.setScene(scene);
        editRecordWindow.show();
    }

    //создать бэкап
    private void backupDatabase() {
        if (db == null || !db.getDbFilePath().exists()) {
            infoLabel.setText("База данных не создана");
        }
        else {
            try {
                String backupPath = "backups" + File.separator + "backup_" + System.currentTimeMillis() + ".csv";
                db.createBackup(backupPath);
                String message = db.getMessage();
                Platform.runLater(() -> infoLabel.setText(message));
            } catch (Exception e) {
                System.out.println("Ошибка при создании бэкапа базы данных: " + e.getMessage());
            }
        }
    }

    //восстановить бэкап
    private void restoreDatabase() {
        try {
            String backupPath = getLatestBackupFileName();
            db.restoreFromBackup(backupPath);
            String message = db.getMessage();
            Platform.runLater(() -> infoLabel.setText(message));
        } catch (IllegalArgumentException e) {
            Platform.runLater(() -> infoLabel.setText("Резервная копия не найдена"));
        }
        catch (Exception e) {
            System.out.println("Ошибка при восстановлении базы данных из бэкапа: " + e.getMessage());
        }
    }

    //вспомогательная для восстановления
    private String getLatestBackupFileName() {
        File backupDir = new File("backups");
        File[] backupFiles = backupDir.listFiles((dir, name) -> name.startsWith("backup_") && name.endsWith(".csv"));
        if (backupFiles == null || backupFiles.length == 0) {
            throw new IllegalArgumentException("Бэкапы не найдены");
        }
        File latestBackup = backupFiles[0];
        for (File file : backupFiles) {
            if (file.lastModified() > latestBackup.lastModified()) {
                latestBackup = file;
            }
        }
        return latestBackup.getPath();
    }

    public static void main(String[] args) {
        launch(args);
    }
}