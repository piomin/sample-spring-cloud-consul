package pl.piomin.services.customer;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit5.HoverflyExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.consul.ConsulContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.piomin.services.customer.model.Customer;
import pl.piomin.services.customer.model.CustomerType;

import java.util.List;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ExtendWith(HoverflyExtension.class)
public class CustomerControllerTests {

    RestTestClient restClient;

    public CustomerControllerTests(WebApplicationContext context) {
        this.restClient = RestTestClient.bindToApplicationContext(context)
                .build();
    }

    @Container
    static ConsulContainer consulContainer = new ConsulContainer("consul:1.15")
            .withConsulCommand("kv put config/customer-service test=abc");

    @BeforeAll
    static void init() {
        System.setProperty("spring.cloud.consul.port", consulContainer.getFirstMappedPort().toString());
        System.setProperty("spring.config.import", "optional:consul:localhost:" + consulContainer.getFirstMappedPort());
    }

    @Test
    void findAll() {
        List<Long> ids = List.of(1L, 2L, 3L);
        restClient.post().uri("/ids").body(ids)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Customer[].class)
                .value(customers -> assertEquals(3, customers.length));
    }

    @Test
    void findById() {
        restClient.get().uri("/{id}", 1L)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Customer.class)
                .value(Assertions::assertNotNull)
                .value(customer -> assertNotNull(customer.getId()));
    }

    @Test
    void add() {
        Customer c = new Customer("John Scott", CustomerType.NEW);
        restClient.post().uri("/").body(c)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Customer.class)
                .value(Assertions::assertNotNull)
                .value(customer -> assertNotNull(customer.getId()));
    }

    @Test
    void findByIdWithAccounts(Hoverfly hoverfly) {
        hoverfly.simulate(
                dsl(service("http://account-service")
                        .get("/customer/1")
                        .willReturn(success().body("[{\"id\":1}]").header("Content-Type", "application/json")))
        );
        restClient.get().uri("/withAccounts/{id}", 1L).exchange()
                .expectStatus().isOk()
                .expectBody(Customer.class)
                .value(Assertions::assertNotNull)
                .value(customer -> assertNotNull(customer.getId()));
    }
    
}
