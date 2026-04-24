package hello.controller;

import hello.declaration.TimeClient;
import hello.model.SimpleTimeClient;
import hello.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@RestController
public class HelloController {

    @Autowired
    private TopicService topicService;

    /**
     * Java 8 Date Time example
     */
    @RequestMapping("/datetime")
    public String index() {
        var myTimeClient = new SimpleTimeClient();
        return """
                Greetings from Spring Boot! ----------------------\
                Datetime now is %s----------------------\
                Datetime tomorrow will be %s----------------------\
                Datetime of previous month was %s----------------------\
                Is this a leap year ?  %s----------------------\
                Default system zone id   %s-------------------\
                Time in California: %s""".formatted(
                myTimeClient,
                myTimeClient.getLocalDateTime().plusDays(1),
                myTimeClient.getLocalDateTime().minus(1, ChronoUnit.MONTHS),
                LocalDate.now().isLeapYear(),
                ZoneId.systemDefault(),
                myTimeClient.getZonedDateTime("Canada/Central"));
    }


    /**
     * String Operations in Java 8
     */
    @RequestMapping("/topic/string/operation")
    public String showStringOperation() {
        var join = topicService.returnAllTopicIDWithStringSlicing();
        var makeDistinctAndSortCharacters = topicService.makeDistinctAndSortCharacters(join);
        var splitResult = topicService
                .splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(join);
        var findIdHavingCharacter = topicService.findIdHavingCharacter();

        return """
                Joining All String ID's with JOIN method: %s\
                -------------Get all ID characters, select distict and sort with ID=   %s\
                -------------Split All Id With Colon,\
                Select ID With "Java" Keyword,\
                 Then Sort Then Join %s\
                -------------Return All ID having character 'g' in it:  %s""".formatted(
                join,
                makeDistinctAndSortCharacters,
                splitResult,
                findIdHavingCharacter);
    }


    /**
     * File Operation in Java 8
     */
    @RequestMapping("/topic/file/operation")
    public String showFileOperation() {
        var findAllFilesInPathAndSort = topicService.findAllFilesInPathAndSort();
        var findParticularFileInPathAndSort = topicService.findParticularFileInPathAndSort();
        var findWithWalk = topicService.findParticularFileInPathAndSortWithWalkFunction();
        var readFile = topicService.readFileWithStreamFunction();
        return """
                ---------Find all files in path and sort:    %s\
                ----------Find File in present directory which strats with "grad",\
                provided maximum depth=25 and sort : %s\
                ----------Find File in present directory which strats with "grad",\
                provided maximum depth=25 and sort :  with walk function%s\
                ---------Read "temp.txt" file with stream functions, having "print" witin it:  %s""".formatted(
                findAllFilesInPathAndSort,
                findParticularFileInPathAndSort,
                findWithWalk,
                readFile);
    }

}
