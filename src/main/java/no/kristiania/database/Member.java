package no.kristiania.database;

public class Member {
    private String name;
    private String lastname;
    private String email;
    private double age;
    private Long id;
    private Long categoryId;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public double getAge() { return age; }

    public Long getId(){ return id; }

    public void setId(Long id){ this.id = id; }

    public void setAge(double age) { this.age = age; }

    public void setEmail(String email) { this.email = email; }

    public void setLastName(String lastName) { this.lastname = lastName; }

    public String getLastName(){ return lastname;}

    public String getEmail() { return email; }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
