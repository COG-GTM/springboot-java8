package hello.controller;

import hello.model.Topic;
import hello.service.TopicService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/topic")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TopicController {

    @Inject
    TopicService topicService;

    @GET
    public List<Topic> getAllTopics() {
        return topicService.getAllTopics();
    }

    @GET
    @Path("/{id}")
    public Topic getTopicWithID(@PathParam("id") String id) {
        return topicService.getTopicWithId(id);
    }

    @POST
    public void addTopic(Topic topic) {
        topicService.addTopic(topic);
    }

    @PUT
    @Path("/{id}")
    public void updateTopic(@PathParam("id") String id, Topic topic) {
        topicService.updateTopic(id, topic);
    }

    @DELETE
    @Path("/{id}")
    public void deleteTopic(@PathParam("id") String id) {
        topicService.deleteTopic(id);
    }

    @GET
    @Path("/minimum/length/{minLength}")
    public List<Topic> filterMinimumLengthForId(@PathParam("minLength") Integer minLength) {
        return topicService.filterMinimumLengthForId(minLength);
    }

    @GET
    @Path("/sort")
    public List<Topic> sortTopicsWithID() {
        return topicService.sortTopicsWithID();
    }

    private static final String joinTemplate = "Joining All String ID's with JOIN method: ";
    private static final String makeDistinctAndSortCharactersTemplate = "-------------Get all ID characters, select distict and sort with ID=   ";
    private static final String splitAllIdWithColonSelectIDWithJavaKeywordThenSortThenJoinTemplate = "-------------Split All Id With Colon," +
            "Select ID With \"Java\" Keyword," +
            " Then Sort Then Join ";
    private static final String findIdHavingCharacterTemplate = "-------------Return All ID having character 'g' in it:  ";
    private static final String findAllFilesInPathAndSortTemplate = "---------Find all files in path and sort:    ";
    private static final String findParticularFileInPathAndSortTemplate = "----------Find File in present directory which strats with \"grad\",provided maximum depth=25 and sort : ";
    private static final String findParticularFileInPathAndSortWithWalkFunctionTemplate = "----------Find File in present directory which strats with \"grad\",provided maximum depth=25 and sort :  with walk function";
    private static final String readFileWithStreamFunctionTemplate = "---------Read \"temp.txt\" file with stream functions, having \"print\" witin it:  ";

    /**
     * String Operations in Java 8
     *
     * @return
     */
    @GET
    @Path("/string/operation")
    @Produces(MediaType.TEXT_PLAIN)
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
    @GET
    @Path("/file/operation")
    @Produces(MediaType.TEXT_PLAIN)
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
