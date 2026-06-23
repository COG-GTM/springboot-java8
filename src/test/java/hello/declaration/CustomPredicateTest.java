package hello.declaration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomPredicateTest {

    @Test
    public void lambdaImplementsFunctionalInterface() {
        CustomPredicate<Integer> greaterThanFive = value -> value > 5;
        assertTrue(greaterThanFive.test(10));
        assertFalse(greaterThanFive.test(3));
    }

    @Test
    public void lambdaWorksWithStrings() {
        CustomPredicate<String> startsWithJava = value -> value.startsWith("java");
        assertTrue(startsWithJava.test("javascript"));
        assertFalse(startsWithJava.test("spring"));
    }
}
