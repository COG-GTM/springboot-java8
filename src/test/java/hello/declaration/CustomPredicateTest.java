package hello.declaration;

import org.junit.Test;

import static org.junit.Assert.*;

public class CustomPredicateTest {

    @Test
    public void test_withLambda_returnsTrue() {
        CustomPredicate<String> isLong = s -> s.length() > 3;
        assertTrue(isLong.test("hello"));
    }

    @Test
    public void test_withLambda_returnsFalse() {
        CustomPredicate<String> isLong = s -> s.length() > 3;
        assertFalse(isLong.test("hi"));
    }

    @Test
    public void test_withIntegerPredicate() {
        CustomPredicate<Integer> isPositive = n -> n > 0;
        assertTrue(isPositive.test(5));
        assertFalse(isPositive.test(-1));
        assertFalse(isPositive.test(0));
    }
}
