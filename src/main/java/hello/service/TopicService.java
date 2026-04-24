package hello.service;

import hello.declaration.CustomPredicate;
import hello.model.Topic;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class TopicService {


    private final List<Topic> topics = new ArrayList<>(List.of(
            new Topic("spring", "Spring Framework", "Spring Framework Description"),
            new Topic("java", "Core Java", "Java Description"),
            new Topic("javascript", "javascript Framework", "javascript Framework Description")
    ));

    public List<Topic> getAllTopics() {
        return topics;
    }

    /**
     * Stream Example
     */
    public Topic getTopicWithId(String id) {
        return topics.stream()
                .filter(topic -> topic.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void addTopic(Topic topic) {
        topics.add(topic);
    }

    /**
     * IntStream examples
     */
    public void updateTopic(String id, Topic topic) {
        var indexOfElement = IntStream.range(0, topics.size())
                .filter(index -> id.equals(topics.get(index).getId()))
                .findFirst();
        if (indexOfElement.isPresent()) {
            topics.set(indexOfElement.getAsInt(), topic);
        }
    }

    /**
     * Lambda Expressions
     */
    public void deleteTopic(String id) {
        topics.removeIf(topic -> topic.getId().equals(id));
    }

    /**
     * Calling functional Interface
     */
    public List<Topic> filterMinimumLengthForId(Integer minLength) {
        return printTopicsWithPredicate(topics, topic -> topic.getId().length() > minLength);
    }


    /**
     * Functional Interface example With ForEach
     */
    private static List<Topic> printTopicsWithPredicate(List<Topic> topicList, CustomPredicate<Topic> tester) {
        var resultTopic = new ArrayList<Topic>();
        topicList.forEach(topic -> {
            if (tester.test(topic)) resultTopic.add(topic);
        });
        return resultTopic;
    }


    /**
     * Using Comparator to sort
     */
    public List<Topic> sortTopicsWithID() {
        topics.sort(Comparator.comparing(Topic::getId));
        return topics;
    }


    /**
     * Join List of Strings
     */
    public String returnAllTopicIDWithStringSlicing() {
        var topicIds = topics.stream()
                .map(Topic::getId)
                .collect(Collectors.toList());
        return String.join(":", topicIds);
    }


    /**
     * Use of MapToObject and distinct
     */
    public String makeDistinctAndSortCharacters(String join) {
        return join.chars().distinct()
                .mapToObj(id -> String.valueOf((char) id))
                .sorted()
                .collect(Collectors.joining());
    }


    /**
     * Use of Pattern Class with stream
     */
    public String splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(String join) {
        return Pattern.compile(":")
                .splitAsStream(join)
                .filter(s -> s.contains("java"))
                .sorted()
                .collect(Collectors.joining(":"));
    }


    /**
     * Apply Regex as Predicate with Stream
     */
    public String findIdHavingCharacter() {
        var pattern = Pattern.compile(".*g.*");
        var topicIdList = topics.stream()
                .map(Topic::getId)
                .toArray(String[]::new);

        return Stream.of(topicIdList)
                .filter(pattern.asPredicate())
                .collect(Collectors.toList())
                .toString();
    }


    /**
     * NIO Java API
     * Use Streams with files
     * Try with resource, "Autoclose"
     */
    public String findAllFilesInPathAndSort() {
        try (var stream = Files.list(Path.of(""))) {
            return stream
                    .map(String::valueOf)
                    .filter(path -> !path.startsWith("."))
                    .sorted()
                    .collect(Collectors.joining("; "));
        } catch (IOException e) {
            return " Error in IO";
        }
    }

    /**
     * Using File.find function to find file
     */
    public String findParticularFileInPathAndSort() {
        var start = Path.of("");
        var maxDepth = 25;
        try (var stream = Files.find(start, maxDepth, (path, attr) ->
                String.valueOf(path).startsWith("grad"))) {
            return stream
                    .sorted()
                    .map(String::valueOf)
                    .collect(Collectors.joining("; "));
        } catch (IOException e) {
            return " IO exception ";
        }
    }


    /**
     * Using Files.Walk Function to find File
     */
    public String findParticularFileInPathAndSortWithWalkFunction() {
        var start = Path.of("");
        var maxDepth = 5;
        try (var stream = Files.walk(start, maxDepth)) {
            return stream
                    .map(String::valueOf)
                    .filter(path -> path.startsWith("grad"))
                    .sorted()
                    .collect(Collectors.joining("; "));
        } catch (IOException e) {
            return " IO exception ";
        }
    }


    /**
     * Use BufferedReader with Stream functions
     */
    public String readFileWithStreamFunction() {
        var path = Path.of("temp.txt");
        try (var reader = Files.newBufferedReader(path)) {
            return reader
                    .lines()
                    .filter(line -> line.contains("print"))
                    .map(line -> line.substring("print".length()))
                    .collect(Collectors.joining(","));
        } catch (IOException e) {
            return " IO exception ";
        }
    }

}
