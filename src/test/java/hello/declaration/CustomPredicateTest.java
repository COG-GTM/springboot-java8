package hello.declaration;

import static org.junit.Assert.*;

import org.junit.Test;

public class CustomPredicateTest {

    @Test
    public void testPredicateTrue() {
        CustomPredicate<String> predicate = s -> s.length() > 3;
        assertTrue(predicate.test("hello"));
    }

    @Test
    public void testPredicateFalse() {
        CustomPredicate<String> predicate = s -> s.length() > 3;
        assertFalse(predicate.test("hi"));
    }

    @Test
    public void testPredicateWithInteger() {
        CustomPredicate<Integer> predicate = n -> n > 10;
        assertTrue(predicate.test(15));
        assertFalse(predicate.test(5));
    }
}
