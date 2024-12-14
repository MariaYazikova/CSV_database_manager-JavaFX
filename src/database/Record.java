package database;
import java.io.Serializable;

//класс для объекта записи
public class Record implements Serializable {
    private int id;
    private String name;
    private double salary;
    private boolean needs_raise;

    public Record(int id, String name, double salary, boolean needs_raise) {
        if (name.length() > 20) {
            throw new IllegalArgumentException("Имя не может быть больше 20 символов");
        }
        this.id = id;
        this.name = name;
        this.salary = salary;
        this.needs_raise = needs_raise;
    }
    public int getId() {
        return id;
    }
    public String getName(){
        return name;
    }
    public double getSalary(){
        return salary;
    }
    public boolean getNeedsRaise(){
        return needs_raise;
    }

    @Override
    public String toString() {
        return "Запись{ id=" + id + ", name='" + name + "', salary=" + salary + ", needs_raise=" + needs_raise + " }";
    }
}