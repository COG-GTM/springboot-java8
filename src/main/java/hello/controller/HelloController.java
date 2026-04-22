package hello.controller;

import hello.declaration.SimpleTimeClient;
import hello.declaration.TimeClient;
import hello.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@RestController
public class HelloController {


    String joinTemplate = "Joining All String ID's with JOIN method: ";
    String makeDistinctAndSortCharactersTemplate = "-------------Get all ID characters, select distict and sort with ID=   ";
    String splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoinTemplate = "-------------Split All Id With Colon," +
            "Select ID With \"Java\" Keyword," +
            " Then Sort Then Join ";
    String findIdHavingCharacterTemplate = "-------------Return All ID having character \'g\' in it:  ";
    String findAllFilesInPathAndSortTemplate = "---------Find all files in path and sort:    ";
    String findParticularFileInPathAndSortTemplate = "----------Find File in present directory which strats with \"grad\",provided maximum depth=25 and sort : ";
    String findParticularFileInPathAndSortWithWalkFunctionTemplate = "----------Find File in present directory which strats with \"grad\",provided maximum depth=25 and sort :  with walk function";
    String readFileWithStreamFunctionTemplate = "---------Read \"temp.txt\" file with stream functions, having \"print\" witin it:  ";


    @Autowired
    private TopicService topicService;

    /**
     * Java 8 Date Time example
     *
     * @return
     */
    @GetMapping("/datetime")
    public String index() {
        TimeClient myTimeClient = new SimpleTimeClient();
        LocalDateTime localDateTime = LocalDateTime.now();
        return "Greetings from Spring Boot! ----------------------" +
                "Datetime now is " + String.valueOf(myTimeClient.toString()) + "----------------------" +
                "Datetime tomorrow will be " + String.valueOf(myTimeClient.getLocalDateTime().plusDays(1)) + "----------------------" +
                "Datetime of previous month was " + String.valueOf(myTimeClient.getLocalDateTime().minus(1, ChronoUnit.MONTHS)) + "----------------------" +
                "Is this a leap year ?  " + String.valueOf(LocalDate.now().isLeapYear()) + "----------------------" +
                "Default system zone id   " + String.valueOf(ZoneId.systemDefault()) + "-------------------" +
                "Time in California: " + myTimeClient.getZonedDateTime("Canada/Central").toString();

    }


    /**
     * String Operations in Java 8
     *
     * @return
     */
    @GetMapping("/topic/string/operation")
    public String showStringOperation() {

        String join = topicService.returnAllTopicIDWithStringSlicing();
        String makeDistinctAndSortCharacters = topicService.makeDistinctAndSortCharacters(join);
        String splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin = topicService
                .splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(join);
        String findIdHavingCharacter = topicService.findIdHavingCharacter();

        return joinTemplate + join
                + makeDistinctAndSortCharactersTemplate + makeDistinctAndSortCharacters
                + splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoinTemplate + splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin
                + findIdHavingCharacterTemplate + findIdHavingCharacter;

    }


    /**
     * File Operation in Java 8
     * @return
     */
    @GetMapping("/topic/file/operation")
    public String showFileOperation() {
        String findAllFilesInPathAndSort = topicService.findAllFilesInPathAndSort();
        String findParticularFileInPathAndSort = topicService.findParticularFileInPathAndSort();
        String findParticularFileInPathAndSortWithWalkFunction = topicService.findParticularFileInPathAndSortWithWalkFunction();
        String readFileWithStreamFunction = topicService.readFileWithStreamFunction();
        return findAllFilesInPathAndSortTemplate + findAllFilesInPathAndSort
                + findParticularFileInPathAndSortTemplate + findParticularFileInPathAndSort
                + findParticularFileInPathAndSortWithWalkFunctionTemplate + findParticularFileInPathAndSortWithWalkFunction
                + readFileWithStreamFunctionTemplate + readFileWithStreamFunction;
    }



}
