package hello.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class Topic {
    public static final int MAX_ID_LENGTH = 64;
    public static final int MAX_SUBJECT_NAME_LENGTH = 128;
    public static final int MAX_SUBJECT_DESCRIPTION_LENGTH = 1024;

    @NotBlank
    @Size(max = MAX_ID_LENGTH)
    private String id;

    @Size(max = MAX_SUBJECT_NAME_LENGTH)
    private String subjectName;

    @Size(max = MAX_SUBJECT_DESCRIPTION_LENGTH)
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
