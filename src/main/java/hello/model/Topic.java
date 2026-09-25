package hello.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "topics")
public class Topic {
    @Id
    private String id;

    @Column(nullable = false)
    private String subjectName;

    @Column(name = "description", nullable = false)
    private String subjectDescription;

    public Topic() {
    }

    public Topic(String id, String subjectName, String subjectDescription) {
        this.id = id;
        this.subjectName = subjectName;
        this.subjectDescription = subjectDescription;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectDescription() {
        return subjectDescription;
    }

    public void setSubjectDescription(String subjectDescription) {
        this.subjectDescription = subjectDescription;
    }

    @Override
    public String toString() {
        return "Topic{id='" + id + "', subjectName='" + subjectName + "', subjectDescription='" + subjectDescription + "'}";
    }
}
