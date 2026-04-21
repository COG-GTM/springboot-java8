package hello.service;

import hello.declaration.CustomPredicate;
import hello.model.Topic;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class TopicService {

    private final List<Topic> topics = new CopyOnWriteArrayList<>(List.of(
            new Topic("spring", "Spring Framework", "Spring Framework Description"),
            new Topic("java", "Core Java", "Java Description"),
            new Topic("javascript", "javascript Framework", "javascript Framework Description")
    ));

    public List<Topic> getAllTopics() {
        return List.copyOf(topics);
    }

    public Optional<Topic> getTopicWithId(String id) {
        return topics.stream().filter(topic -> topic.id().equals(id)).findFirst();
    }

    public void addTopic(Topic topic) {
        topics.add(topic);
    }

    public void updateTopic(String id, Topic topic) {
        OptionalInt indexOfElement = IntStream.range(0, topics.size())
                .filter(index -> id.equals(topics.get(index).id()))
                .findFirst();
        if (indexOfElement.isPresent()) {
            topics.set(indexOfElement.getAsInt(), topic);
        }
    }

    public void deleteTopic(String id) {
        topics.removeIf(topic -> topic.id().equals(id));
    }

    public List<Topic> filterMinimumLengthForId(Integer minLength) {
        return filterTopicsWithPredicate(topics, topic -> topic.id().length() > minLength);
    }

    private static List<Topic> filterTopicsWithPredicate(List<Topic> topicList, CustomPredicate<Topic> tester) {
        List<Topic> resultTopic = new ArrayList<>();
        topicList.forEach(topic -> {
            if (tester.test(topic)) {
                resultTopic.add(topic);
            }
        });
        return resultTopic;
    }

    public List<Topic> sortTopicsWithID() {
        return topics.stream()
                .sorted(Comparator.comparing(Topic::id))
                .toList();
    }

    public String returnAllTopicIDWithStringSlicing() {
        return topics.stream()
                .map(Topic::id)
                .collect(java.util.stream.Collectors.joining(":"));
    }

    public String makeDistinctAndSortCharacters(String join) {
        return join.chars().distinct()
                .mapToObj(id -> String.valueOf((char) id))
                .sorted()
                .collect(java.util.stream.Collectors.joining());
    }

    public String splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(String join) {
        return Pattern.compile(":")
                .splitAsStream(join)
                .filter(s -> s.contains("java"))
                .sorted()
                .collect(java.util.stream.Collectors.joining(":"));
    }

    public String findIdHavingCharacter() {
        Pattern pattern = Pattern.compile(".*g.*");
        return topics.stream()
                .map(Topic::id)
                .filter(pattern.asMatchPredicate())
                .toList()
                .toString();
    }

    public String findAllFilesInPathAndSort() {
        try (Stream<Path> stream = Files.list(Paths.get(""))) {
            return stream
                    .map(String::valueOf)
                    .filter(path -> !path.startsWith("."))
                    .sorted()
                    .collect(java.util.stream.Collectors.joining("; "));
        } catch (IOException e) {
            return " Error in IO";
        }
    }

    public String findParticularFileInPathAndSort() {
        Path start = Paths.get("");
        int maxDepth = 25;
        try (Stream<Path> stream = Files.find(start, maxDepth, (path, attr) ->
                String.valueOf(path).startsWith("pom"))) {
            return stream
                    .sorted()
                    .map(String::valueOf)
                    .collect(java.util.stream.Collectors.joining("; "));
        } catch (IOException e) {
            return " IO exception ";
        }
    }

    public String findParticularFileInPathAndSortWithWalkFunction() {
        Path start = Paths.get("");
        int maxDepth = 5;
        try (Stream<Path> stream = Files.walk(start, maxDepth)) {
            return stream
                    .map(String::valueOf)
                    .filter(path -> path.startsWith("pom"))
                    .sorted()
                    .collect(java.util.stream.Collectors.joining("; "));
        } catch (IOException e) {
            return " IO exception ";
        }
    }

    public String readFileWithStreamFunction() {
        Path path = Paths.get("README.md");
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return reader.lines()
                    .filter(line -> line.contains("spring"))
                    .collect(java.util.stream.Collectors.joining(","));
        } catch (IOException e) {
            return " IO exception ";
        }
    }

}
