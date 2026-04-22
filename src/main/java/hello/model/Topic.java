package hello.model;

import java.util.Objects;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class Topic {
    @NotBlank
    private String id;

    @NotBlank
    private String subjectName;

    @Size(max = 500)
    private String subjectDescription;

    public Topic() {
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

    public Topic(String id, String subjectName, String subjectDescription) {
        super();
        this.id = id;
        this.subjectName = subjectName;
        this.subjectDescription = subjectDescription;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "id='" + id + '\'' +
                ", subjectName='" + subjectName + '\'' +
                ", subjectDescription='" + subjectDescription + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Topic topic = (Topic) o;
        return Objects.equals(id, topic.id)
                && Objects.equals(subjectName, topic.subjectName)
                && Objects.equals(subjectDescription, topic.subjectDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, subjectName, subjectDescription);
    }

}
