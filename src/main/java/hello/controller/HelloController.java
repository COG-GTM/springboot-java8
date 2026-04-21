package hello.controller;

import hello.declaration.TimeClient;
import hello.model.SimpleTimeClient;
import hello.service.TopicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@RestController
public class HelloController {

    private static final String JOIN_TEMPLATE = "Joining All String ID's with JOIN method: ";
    private static final String DISTINCT_SORTED_TEMPLATE = "-------------Get all ID characters, select distict and sort with ID=   ";
    private static final String SPLIT_JAVA_KEYWORD_TEMPLATE = "-------------Split All Id With Colon,Select ID With \"Java\" Keyword, Then Sort Then Join ";
    private static final String FIND_ID_HAVING_CHARACTER_TEMPLATE = "-------------Return All ID having character 'g' in it:  ";
    private static final String FIND_ALL_FILES_TEMPLATE = "---------Find all files in path and sort:    ";
    private static final String FIND_PARTICULAR_FILE_TEMPLATE = "----------Find File in present directory which starts with \"pom\", provided maximum depth=25 and sort : ";
    private static final String FIND_PARTICULAR_FILE_WALK_TEMPLATE = "----------Find File in present directory which starts with \"pom\", provided maximum depth=25 and sort : with walk function ";
    private static final String READ_FILE_TEMPLATE = "---------Read \"README.md\" file with stream functions, having \"spring\" within it:  ";

    private final TopicService topicService;

    public HelloController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping("/datetime")
    public String datetime() {
        TimeClient myTimeClient = new SimpleTimeClient();
        LocalDateTime now = myTimeClient.getLocalDateTime();
        return """
               Greetings from Spring Boot! ----------------------\
               Datetime now is %s----------------------\
               Datetime tomorrow will be %s----------------------\
               Datetime of previous month was %s----------------------\
               Is this a leap year ? %s----------------------\
               Default system zone id %s-------------------\
               Time in Canada/Central: %s""".formatted(
                myTimeClient,
                now.plusDays(1),
                now.minus(1, ChronoUnit.MONTHS),
                LocalDate.now().isLeapYear(),
                ZoneId.systemDefault(),
                myTimeClient.getZonedDateTime("Canada/Central"));
    }

    @GetMapping("/topic/string/operation")
    public String showStringOperation() {
        String join = topicService.returnAllTopicIDWithStringSlicing();
        String distinct = topicService.makeDistinctAndSortCharacters(join);
        String split = topicService.splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(join);
        String findG = topicService.findIdHavingCharacter();
        return JOIN_TEMPLATE + join
                + DISTINCT_SORTED_TEMPLATE + distinct
                + SPLIT_JAVA_KEYWORD_TEMPLATE + split
                + FIND_ID_HAVING_CHARACTER_TEMPLATE + findG;
    }

    @GetMapping("/topic/file/operation")
    public String showFileOperation() {
        return FIND_ALL_FILES_TEMPLATE + topicService.findAllFilesInPathAndSort()
                + FIND_PARTICULAR_FILE_TEMPLATE + topicService.findParticularFileInPathAndSort()
                + FIND_PARTICULAR_FILE_WALK_TEMPLATE + topicService.findParticularFileInPathAndSortWithWalkFunction()
                + READ_FILE_TEMPLATE + topicService.readFileWithStreamFunction();
    }

}
