package pl.piomin.services.account;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.consul.ConsulContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.piomin.services.account.model.Account;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureRestTestClient
public class AccountControllerTests {

    @Autowired
    RestTestClient restClient;

    @Container
    static ConsulContainer consulContainer = new ConsulContainer("consul:1.15")
            .withConsulCommand("kv put config/account-service test=abc");

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
                .expectBody(Account[].class)
                .value(accounts -> assertEquals(3, accounts.length));
    }

    @Test
    void findByCustomerId() {
        restClient.get().uri("/customer/{customerId}", 1L)
                .exchange()
                .expectBody(Account[].class)
                .value(accounts -> assertTrue(accounts.length > 0));
    }

    @Test
    void add() {
        Account a = new Account("2234567890", 50000, 10L);
        restClient.post().uri("/").body(a)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Account.class)
                .value(Assertions::assertNotNull)
                .value(account -> assertNotNull(account.getId()));
    }

    @Test
    void withdrawOk() {
        restClient.put().uri("/withdraw/1/10000")
                .exchange()
                .expectBody(Account.class)
                .value(Assertions::assertNotNull)
                .value(account -> assertEquals(40000, account.getBalance()));
    }

    @Test
    void withdrawFailed() {
        restClient.put().uri("/withdraw/2/100000")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
