package database;

import java.io.*;
import java.util.*;

public class Database {
    private File db_file;
    private Map<Integer, Long> index;
    private Map<String, LinkedHashSet<Long>> name_index;
    private Map<Double, LinkedHashSet<Long>> salary_index;
    private Map<Boolean, LinkedHashSet<Long>> needs_raise_index;
    String message;

    //создание бд (конструктор с параметром)
    public Database(String file_path) throws IOException {
        db_file = new File(file_path);
        index = new HashMap<>();
        name_index = new HashMap<>();
        salary_index = new HashMap<>();
        needs_raise_index = new HashMap<>();

        if (!db_file.exists()) {
            db_file.createNewFile();
            try (FileWriter writer = new FileWriter(db_file)) {
                writer.write("ID,Name,Salary,NeedsRaise\n");
            }
            message = "База данных успешно создана";
        }
        else {
            initializeIndexes();
            message = "База данных уже была создана ранее";
        }
        this.message = message;
    }

    //инициализация индексов в хеш таблицах
    public void initializeIndexes() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(db_file))) {
            String line;
            long position = 0;
            String line_one;
            line_one = reader.readLine();
            position += line_one.length() + 1;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] fields = line.split(",");
                    String pr_str = fields[0];
                    if (pr_str.matches("^x+$")) {
                        position += line.length() + 1;
                        continue;
                    }
                    int id = Integer.parseInt(fields[0].trim());
                    String name = fields[1].trim();
                    double salary = Double.parseDouble(fields[2].trim());
                    boolean needsRaise = Boolean.parseBoolean(fields[3].trim());
                    index.put(id, position);
                    name_index.computeIfAbsent(name, k -> new LinkedHashSet<>()).add(position);
                    salary_index.computeIfAbsent(salary, k -> new LinkedHashSet<>()).add(position);
                    needs_raise_index.computeIfAbsent(needsRaise, k -> new LinkedHashSet<>()).add(position);
                    position += line.length() + 1;
                } catch (Exception e) {
                    System.err.println("Ошибка обработки строки: " + line);
                    e.printStackTrace();
                }
            }
        }
    }

    //получение сообщения
    public String getMessage() {
        return message;
    }

    //возврат файла
    public File getDbFilePath() {
        return db_file;
    }

    //удаление бд
    public void deleteDatabase() {
        if (db_file.exists()) {
            if (db_file.delete()) {
                index = new HashMap<>();
                name_index = new HashMap<>();
                salary_index = new HashMap<>();
                needs_raise_index = new HashMap<>();
                message = "База данных успешно удалена";
            } else {
                System.err.println("Не удалось удалить базу данных");
            }
        } else {
            message = "База данных уже была удалена ранее";
        }
    }

    //очистка содержимого бд
    public void clearDatabase() {
        if (db_file.exists()) {
            try (FileWriter writer = new FileWriter(db_file, false)) {
                writer.write("ID,Name,Salary,NeedsRaise\n");
            } catch (IOException e) {
                System.err.println("Не удалось очистить базу данных: " + e.getMessage());
            }
            index = new HashMap<>();
            name_index = new HashMap<>();
            salary_index = new HashMap<>();
            needs_raise_index = new HashMap<>();
            message = "База данных успешно очищена";
        }
        else{
            message = "База данных не создана";
        }
    }

    //добавление новой записи
    public void addRecord(Record record) throws IOException {
        if (index.containsKey(record.getId())) {
            throw new IllegalArgumentException("Запись с таким ID уже существует!");
        }
        try (FileWriter writer = new FileWriter(db_file, true)) {
            long position = new File(db_file.getAbsolutePath()).length();
            String recordLine = String.format("%d,%s,%s,%b\n",
                    record.getId(),
                    record.getName(),
                    record.getSalary(),
                    record.getNeedsRaise()
            );
            writer.write(recordLine);
            index.put(record.getId(), position);
            name_index.computeIfAbsent(record.getName(), k -> new LinkedHashSet<>()).add(position);
            salary_index.computeIfAbsent(record.getSalary(), k -> new LinkedHashSet<>()).add(position);
            needs_raise_index.computeIfAbsent(record.getNeedsRaise(), k -> new LinkedHashSet<>()).add(position);
        }
    }

    //удаление записи по айди
    public void deleteRecord(int id) throws IOException {
        Long position = index.get(id);
        if (position == null) {
            throw new IllegalArgumentException("Совпадения не найдены");
        }
        RandomAccessFile ra_file = new RandomAccessFile(db_file, "rw");
        ra_file.seek(position);
        String line = ra_file.readLine();
        String[] fields = line.split(",");
        String pr_str = fields[0];
        int x_quantity = pr_str.length();
        String x_str = "x".repeat(x_quantity);
        fields[0] = x_str;
        ra_file.seek(position);
        ra_file.writeBytes(String.join(",", fields) + "\n");
        index.remove(id);
    }

    //удаление записей по имени
    public void deleteByName(String name) throws IOException {
        deleteByField(name_index.remove(name));
    }

    //удаление записей по зарплате
    public void deleteBySalary(double salary) throws IOException {
        deleteByField(salary_index.remove(salary));
    }

    //удаление записей по запросу на повышение
    public void deleteByNeedsRaise(boolean needsRaise) throws IOException {
        deleteByField(needs_raise_index.remove(needsRaise));
    }

    //вспомогательный метод для удаления по неключевым полям O(k), где k - кол-во позиций
    private void deleteByField(Set<Long> positions) throws IOException {
        if (positions == null) {
            throw new IllegalArgumentException("Совпадения не найдены");
        }

        try (RandomAccessFile ra_file = new RandomAccessFile(db_file, "rw")) {
            for (Long position : positions) {
                ra_file.seek(position);
                String line = ra_file.readLine();
                String[] fields = line.split(",");
                String pr_str = fields[0];
                if (pr_str.matches("^x+$")) {
                    continue;
                }
                int x_quantity = pr_str.length();
                String x_str = "x".repeat(x_quantity);
                fields[0] = x_str;
                ra_file.seek(position);
                ra_file.writeBytes(String.join(",", fields) + "\n");
                index.values().remove(position);
            }
        }
    }

    //поиск записи по айди ключу O(1)
    public Record searchById(int id) throws IOException, ClassNotFoundException {
        Long position = index.get(id);
        if (position == null) {
            return null;
        }
        try (RandomAccessFile ra_file = new RandomAccessFile(db_file, "r")) {
            ra_file.seek(position);
            String line = ra_file.readLine();
            if (line != null) {
                String[] fields = line.split(",");
                int recordId = Integer.parseInt(fields[0].trim());
                String name = fields[1].trim();
                double salary = Double.parseDouble(fields[2].trim());
                boolean needsRaise = Boolean.parseBoolean(fields[3].trim());
                Record record = new Record(recordId, name, salary, needsRaise);
                return record;
            }
        }
        return null;
    }

    //поиск записей по имени
    public List<Record> searchByName(String name) throws IOException, ClassNotFoundException {
        return searchByField(name_index.get(name));
    }

    //поиск записей по зарплате
    public List<Record> searchBySalary(double salary) throws IOException, ClassNotFoundException {
        return searchByField(salary_index.get(salary));
    }

    //поиск записей по запросу на повышение
    public List<Record> searchByNeedsRaise(boolean needsRaise) throws IOException, ClassNotFoundException {
        return searchByField(needs_raise_index.get(needsRaise));
    }

    //вспомогательный метод для поиска по неключевым полям
    private List<Record> searchByField(Set<Long> positions) throws IOException {
        if (positions == null) {
            return Collections.emptyList();
        }
        List<Record> results = new ArrayList<>();
        try (RandomAccessFile ra_file = new RandomAccessFile(db_file, "r")) {
            for (Long position : positions) {
                ra_file.seek(position);
                String line = ra_file.readLine();
                String[] fields = line.split(",");
                String pr_str = fields[0];
                if (pr_str.matches("^x+$")) {
                    continue;
                }
                int id = Integer.parseInt(fields[0].trim());
                String name = fields[1].trim();
                double salary = Double.parseDouble(fields[2].trim());
                boolean needsRaise = Boolean.parseBoolean(fields[3].trim());
                Record record = new Record(id, name, salary, needsRaise);
                results.add(record);
            }
        }
        return results;
    }

    // новый метод для вывода всего .csv файла
    public List<String> displayDatabaseContents() throws IOException {
        if (!db_file.exists()) {
            return null;
        }
        List<String> contents = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(db_file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (!fields[0].matches("^x+$")) {
                    contents.add(line);
                }
            }
        }
        return contents;
    }

    public void createBackup(String backupPath) throws IOException {
        File backupFile = new File(backupPath);
        try (InputStream in = new FileInputStream(db_file);
             OutputStream out = new FileOutputStream(backupFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            message = "Резервная копия успешно создана";
        } catch (IOException e) {
            message = e.getMessage();
            throw e;
        }
    }

    public void restoreFromBackup(String backupPath) throws IOException {
        File backupFile = new File(backupPath);
        if (backupFile.exists()) {
            try (InputStream in = new FileInputStream(backupFile);
                 OutputStream out = new FileOutputStream(db_file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                initializeIndexes();
                message = "Резервная копия успешно восстановлена";
            } catch (IOException e) {
                message = e.getMessage();
                throw e;
            }
        } else {
            message = "Резервная копия не найдена";
        }
    }

    public void editRecord(int id, String newName, double newSalary, boolean newNeedsRaise) throws IOException {
        Long position = index.get(id);
        if (position == null) {
            throw new IllegalArgumentException("Совпадения не найдены");
        }
        try (RandomAccessFile ra_file = new RandomAccessFile(db_file, "rw")) {
            ra_file.seek(position);
            String line = ra_file.readLine();
            int oldLineLength = line.length();
            String updatedLine = String.format("%d,%s,%s,%b", id, newName, newSalary, newNeedsRaise);
            if (updatedLine.length() < oldLineLength) {
                updatedLine = String.format("%-" + oldLineLength + "s", updatedLine);
            }
            updatedLine += "\n";
            ra_file.seek(position);
            ra_file.writeBytes(updatedLine);
            initializeIndexes();
        }
    }
}