package hello.model;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TopicValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeClass
    public static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterClass
    public static void closeValidator() {
        factory.close();
    }

    private static String repeat(char c, int count) {
        return String.join("", Collections.nCopies(count, String.valueOf(c)));
    }

    @Test
    public void acceptsIdAtMaximumLength() {
        Topic topic = new Topic(repeat('a', Topic.MAX_ID_LENGTH), "name", "description");
        assertTrue(validator.validate(topic).isEmpty());
    }

    @Test
    public void rejectsIdLongerThanMaximum() {
        Topic topic = new Topic(repeat('a', Topic.MAX_ID_LENGTH + 1), "name", "description");
        Set<ConstraintViolation<Topic>> violations = validator.validate(topic);
        assertEquals(1, violations.size());
        assertEquals("id", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void rejectsMegabyteId() {
        Topic topic = new Topic(repeat('a', 1024 * 1024), "name", "description");
        assertEquals(1, validator.validate(topic).size());
    }

    @Test
    public void rejectsMissingOrBlankId() {
        assertEquals(1, validator.validate(new Topic(null, "name", "description")).size());
        assertEquals(1, validator.validate(new Topic("  ", "name", "description")).size());
    }

    @Test
    public void rejectsOversizedSubjectFields() {
        Topic topic = new Topic("id",
                repeat('n', Topic.MAX_SUBJECT_NAME_LENGTH + 1),
                repeat('d', Topic.MAX_SUBJECT_DESCRIPTION_LENGTH + 1));
        assertEquals(2, validator.validate(topic).size());
    }
}
