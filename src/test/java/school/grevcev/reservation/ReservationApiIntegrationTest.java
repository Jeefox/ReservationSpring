package school.grevcev.reservation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class ReservationApiIntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fullBookingFlow_conflictReturns409() throws Exception {
        mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Integ\",\"email\":\"integ@test.com\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/rooms").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"integ-room\",\"capacity\":2}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"roomId\":1,\"startDate\":\"2027-01-10\",\"endDate\":\"2027-01-15\",\"status\":\"PENDING\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"roomId\":1,\"startDate\":\"2027-01-14\",\"endDate\":\"2027-01-20\",\"status\":\"PENDING\"}"))
                .andExpect(status().isConflict());
    }
}
