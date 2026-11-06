package hello.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class QuoteTest {

    @Test
    public void toStringReplacesLineBreaksAndControlCharacters() {
        Value value = new Value();
        value.setId(7L);
        value.setQuote("ok\r\n2026-11-06 INFO --- [main] hello.Application : admin login OK\u2028\u0007");
        Quote quote = new Quote();
        quote.setType("success\nFAKE");
        quote.setValue(value);

        String logged = quote.toString();

        assertFalse(logged.matches("(?s).*[\\r\\n\\u2028\\u2029\\u0007].*"));
        assertEquals("Quote{type='success_FAKE', value=Value{id=7, quote='ok__2026-11-06 INFO --- [main]"
                + " hello.Application : admin login OK__'}}", logged);
    }

    @Test
    public void toStringKeepsOrdinaryTextAndNulls() {
        Value value = new Value();
        value.setId(1L);
        value.setQuote("Working with Spring Boot is like pair-programming with the Spring developers.");
        Quote quote = new Quote();
        quote.setType("success");
        quote.setValue(value);

        assertEquals("Quote{type='success', value=Value{id=1, quote='Working with Spring Boot is like"
                + " pair-programming with the Spring developers.'}}", quote.toString());
        assertEquals("Quote{type='null', value=null}", new Quote().toString());
    }
}
