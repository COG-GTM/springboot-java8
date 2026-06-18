package hello.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class Topic {
    @NotBlank(message = "ID is required")
    @Size(max = 50, message = "ID must be at most 50 characters")
    private String id;

    @NotBlank(message = "Subject name is required")
    @Size(max = 200, message = "Subject name must be at most 200 characters")
    private String subjectName;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
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

}
