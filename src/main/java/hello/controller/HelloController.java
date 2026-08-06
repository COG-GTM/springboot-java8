package hello.controller;

import hello.declaration.TimeClient;
import hello.model.SimpleTimeClient;
import hello.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@RestController
public class HelloController {

    private static final String JOIN_TEMPLATE = "Joining All String ID's with JOIN method: ";
    private static final String MAKE_DISTINCT_AND_SORT_CHARACTERS_TEMPLATE = "-------------Get all ID characters, select distict and sort with ID=   ";
    private static final String SPLIT_ALL_ID_WITH_COLON_SELECT_ID_WITH_JAVA_KEYWORD_THEN_SORT_THEN_JOIN_TEMPLATE = """
            -------------Split All Id With Colon,\
            Select ID With "Java" Keyword,\
             Then Sort Then Join\s""";
    private static final String FIND_ID_HAVING_CHARACTER_TEMPLATE = "-------------Return All ID having character 'g' in it:  ";
    private static final String FIND_ALL_FILES_IN_PATH_AND_SORT_TEMPLATE = "---------Find all files in path and sort:    ";
    private static final String FIND_PARTICULAR_FILE_IN_PATH_AND_SORT_TEMPLATE = "----------Find File in present directory which strats with \"grad\",provided maximum depth=25 and sort : ";
    private static final String FIND_PARTICULAR_FILE_IN_PATH_AND_SORT_WITH_WALK_FUNCTION_TEMPLATE = "----------Find File in present directory which strats with \"grad\",provided maximum depth=25 and sort :  with walk function";
    private static final String READ_FILE_WITH_STREAM_FUNCTION_TEMPLATE = "---------Read \"temp.txt\" file with stream functions, having \"print\" witin it:  ";

    /** Responses are single line, hence the trailing line-continuation escapes. */
    private static final String LABELLED_SECTIONS_FORMAT = """
            %s%s\
            %s%s\
            %s%s\
            %s%s\
            """;

    @Autowired
    private TopicService topicService;

    /**
     * Java 8 Date Time example
     *
     * @return
     */
    @RequestMapping("/datetime")
    public String index() {
        TimeClient myTimeClient = new SimpleTimeClient();
        return """
                Greetings from Spring Boot! ----------------------\
                Datetime now is %s----------------------\
                Datetime tomorrow will be %s----------------------\
                Datetime of previous month was %s----------------------\
                Is this a leap year ?  %s----------------------\
                Default system zone id   %s-------------------\
                Time in California: %s\
                """.formatted(
                myTimeClient,
                myTimeClient.getLocalDateTime().plusDays(1),
                myTimeClient.getLocalDateTime().minus(1, ChronoUnit.MONTHS),
                LocalDate.now().isLeapYear(),
                ZoneId.systemDefault(),
                myTimeClient.getZonedDateTime("Canada/Central"));
    }

    /**
     * String Operations in Java 8
     *
     * @return
     */
    @RequestMapping("/topic/string/operation")
    public String showStringOperation() {

        String join = topicService.returnAllTopicIDWithStringSlicing();
        String makeDistinctAndSortCharacters = topicService.makeDistinctAndSortCharacters(join);
        String splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin = topicService
                .splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(join);
        String findIdHavingCharacter = topicService.findIdHavingCharacter();

        return LABELLED_SECTIONS_FORMAT.formatted(
                JOIN_TEMPLATE, join,
                MAKE_DISTINCT_AND_SORT_CHARACTERS_TEMPLATE, makeDistinctAndSortCharacters,
                SPLIT_ALL_ID_WITH_COLON_SELECT_ID_WITH_JAVA_KEYWORD_THEN_SORT_THEN_JOIN_TEMPLATE, splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin,
                FIND_ID_HAVING_CHARACTER_TEMPLATE, findIdHavingCharacter);
    }

    /**
     * File Operation in Java 8
     *
     * @return
     */
    @RequestMapping("/topic/file/operation")
    public String showFileOperation() {
        String findAllFilesInPathAndSort = topicService.findAllFilesInPathAndSort();
        String findParticularFileInPathAndSort = topicService.findParticularFileInPathAndSort();
        String findParticularFileInPathAndSortWithWalkFunction = topicService.findParticularFileInPathAndSortWithWalkFunction();
        String readFileWithStreamFunction = topicService.readFileWithStreamFunction();

        return LABELLED_SECTIONS_FORMAT.formatted(
                FIND_ALL_FILES_IN_PATH_AND_SORT_TEMPLATE, findAllFilesInPathAndSort,
                FIND_PARTICULAR_FILE_IN_PATH_AND_SORT_TEMPLATE, findParticularFileInPathAndSort,
                FIND_PARTICULAR_FILE_IN_PATH_AND_SORT_WITH_WALK_FUNCTION_TEMPLATE, findParticularFileInPathAndSortWithWalkFunction,
                READ_FILE_WITH_STREAM_FUNCTION_TEMPLATE, readFileWithStreamFunction);
    }

}
