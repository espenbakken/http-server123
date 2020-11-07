package no.kristiania.database;

public class Member {
    private String name;
    private String lastname;
    private String email;
    private double age;
    private Integer id;
    private Integer taskId;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public double getAge() { return age; }

    public Integer getId(){ return id; }

    public void setId(Integer id){ this.id = id; }

    public void setAge(double age) { this.age = age; }

    public void setEmail(String email) { this.email = email; }

    public void setLastName(String lastName) { this.lastname = lastName; }

    public String getLastName(){ return lastname;}

    public String getEmail() { return email; }

    public Integer gettaskId() {
        return taskId;
    }

    public void settaskId(Integer taskId) {
        this.taskId = taskId;
    }
}
