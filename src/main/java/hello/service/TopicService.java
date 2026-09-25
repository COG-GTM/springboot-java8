package hello.service;

import hello.declaration.CustomPredicate;
import hello.model.Topic;
import hello.repository.TopicRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TopicService {

    private final TopicRepository topicRepository;
    private final JdbcTemplate jdbcTemplate;

    public TopicService(TopicRepository topicRepository, JdbcTemplate jdbcTemplate) {
        this.topicRepository = topicRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Topic> getAllTopics() {
        return topicRepository.findAll();
    }

    /**
     * Optional example
     */
    public Optional<Topic> getTopicWithId(String id) {
        return topicRepository.findById(id);
    }

    public Topic addTopic(Topic topic) {
        return topicRepository.save(topic);
    }

    public Optional<Topic> updateTopic(String id, Topic topic) {
        return topicRepository.findById(id).map(existing -> {
            existing.setSubjectName(topic.getSubjectName());
            existing.setSubjectDescription(topic.getSubjectDescription());
            return topicRepository.save(existing);
        });
    }

    /**
     * Lambda expression
     */
    public void deleteTopic(String id) {
        topicRepository.findById(id).ifPresent(topicRepository::delete);
    }

    /**
     * Calling functional interface
     */
    public List<Topic> filterMinimumLengthForId(Integer minLength) {
        return filterTopicsWithPredicate(getAllTopics(), topic -> topic.getId().length() > minLength);
    }

    private static List<Topic> filterTopicsWithPredicate(List<Topic> topicList, CustomPredicate<Topic> tester) {
        List<Topic> resultTopic = new ArrayList<>();
        topicList.forEach(topic -> {
            if (tester.test(topic)) resultTopic.add(topic);
        });
        return resultTopic;
    }

    /**
     * Using Comparator to sort
     */
    public List<Topic> sortTopicsWithID() {
        return getAllTopics().stream().sorted(Comparator.comparing(Topic::getId)).collect(Collectors.toList());
    }

    /**
     * Cross-component report: reads the DB-side aggregation the reporting job also consumes.
     */
    public List<Map<String, Object>> topicReport() {
        return jdbcTemplate.queryForList("SELECT * FROM topic_report()");
    }

    public String returnAllTopicIDWithStringSlicing() {
        List<String> topicIds = getAllTopics().stream().map(Topic::getId).collect(Collectors.toList());
        return String.join(":", topicIds);
    }

    public String makeDistinctAndSortCharacters(String join) {
        return join.chars().distinct()
                .mapToObj(id -> String.valueOf((char) id))
                .sorted()
                .collect(Collectors.joining());
    }

    public String splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoin(String join) {
        return Pattern.compile(":")
                .splitAsStream(join)
                .filter(s -> s.contains("java"))
                .sorted()
                .collect(Collectors.joining(":"));
    }

    public String findIdHavingCharacter() {
        Pattern pattern = Pattern.compile(".*g.*");
        String[] topicIdList = getAllTopics().stream().map(Topic::getId).toArray(String[]::new);
        return Stream.of(topicIdList)
                .filter(pattern.asPredicate())
                .collect(Collectors.toList())
                .toString();
    }

    public String findAllFilesInPathAndSort() {
        try (Stream<Path> stream = Files.list(Paths.get(""))) {
            return stream
                    .map(String::valueOf)
                    .filter(path -> !path.startsWith("."))
                    .sorted()
                    .collect(Collectors.joining("; "));
        } catch (IOException e) {
            return " Error in IO";
        }
    }

    public String findParticularFileInPathAndSort() {
        try (Stream<Path> stream = Files.find(Paths.get(""), 25, (path, attr) ->
                String.valueOf(path).startsWith("grad"))) {
            return stream.sorted().map(String::valueOf).collect(Collectors.joining("; "));
        } catch (IOException e) {
            return " IO exception ";
        }
    }

    public String findParticularFileInPathAndSortWithWalkFunction() {
        try (Stream<Path> stream = Files.walk(Paths.get(""), 5)) {
            return stream
                    .map(String::valueOf)
                    .filter(path -> path.startsWith("grad"))
                    .sorted()
                    .collect(Collectors.joining("; "));
        } catch (IOException e) {
            return " IO exception ";
        }
    }

    public String readFileWithStreamFunction() {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("temp.txt"))) {
            return reader.lines()
                    .filter(line -> line.contains("print"))
                    .collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            return " IO exception ";
        }
    }
}
