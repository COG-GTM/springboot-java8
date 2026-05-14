package hello.declaration;

import org.junit.Test;
import static org.junit.Assert.*;

public class CustomPredicateTest {

    @Test
    public void testPredicateWithString() {
        CustomPredicate<String> isLong = s -> s.length() > 5;
        assertTrue(isLong.test("longstring"));
        assertFalse(isLong.test("hi"));
    }

    @Test
    public void testPredicateWithInteger() {
        CustomPredicate<Integer> isPositive = i -> i > 0;
        assertTrue(isPositive.test(5));
        assertFalse(isPositive.test(-1));
    }
}
