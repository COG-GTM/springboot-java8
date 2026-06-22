package hello;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import hello.model.Customer;

@ApplicationScoped
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Inject
    DataSource dataSource;

    void onStart(@Observes StartupEvent ev) {
        log.info("Creating tables");
        try (Connection conn = dataSource.getConnection()) {
            Statement stmt = conn.createStatement();
            stmt.execute("DROP TABLE IF EXISTS customers");
            stmt.execute("CREATE TABLE customers(id IDENTITY, first_name VARCHAR(255), last_name VARCHAR(255))");

            List<String[]> splitUpNames = Arrays.asList("John Woo", "Jeff Dean", "Josh Bloch", "Josh Long")
                    .stream()
                    .map(name -> name.split(" "))
                    .collect(Collectors.toList());

            PreparedStatement ps = conn.prepareStatement("INSERT INTO customers(first_name, last_name) VALUES (?,?)");
            for (String[] name : splitUpNames) {
                log.info(String.format("Inserting customer record for %s %s", name[0], name[1]));
                ps.setString(1, name[0]);
                ps.setString(2, name[1]);
                ps.addBatch();
            }
            ps.executeBatch();

            log.info("Querying for customer records where first_name = 'Josh':");
            ResultSet rs = stmt.executeQuery(
                    "SELECT id, first_name, last_name FROM customers WHERE first_name = 'Josh'");
            while (rs.next()) {
                log.info(new Customer(rs.getLong("id"), rs.getString("first_name"),
                        rs.getString("last_name")).toString());
            }
        } catch (SQLException e) {
            log.error("Database error", e);
        }
    }
}
