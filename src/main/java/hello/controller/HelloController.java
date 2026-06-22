package hello.controller;

import hello.declaration.TimeClient;
import hello.model.SimpleTimeClient;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Path("/")
@Produces(MediaType.TEXT_PLAIN)
public class HelloController {

    /**
     * Java 8 Date Time example
     *
     * @return
     */
    @GET
    @Path("/datetime")
    public String index() {
        TimeClient myTimeClient = new SimpleTimeClient();
        LocalDateTime localDateTime = LocalDateTime.now();
        return "Greetings from Quarkus! ----------------------" +
                "Datetime now is " + String.valueOf(myTimeClient.toString()) + "----------------------" +
                "Datetime tomorrow will be " + String.valueOf(myTimeClient.getLocalDateTime().plusDays(1)) + "----------------------" +
                "Datetime of previous month was " + String.valueOf(myTimeClient.getLocalDateTime().minus(1, ChronoUnit.MONTHS)) + "----------------------" +
                "Is this a leap year ?  " + String.valueOf(LocalDate.now().isLeapYear()) + "----------------------" +
                "Default system zone id   " + String.valueOf(ZoneId.systemDefault()) + "-------------------" +
                "Time in California: " + myTimeClient.getZonedDateTime("Canada/Central").toString();

    }

}
