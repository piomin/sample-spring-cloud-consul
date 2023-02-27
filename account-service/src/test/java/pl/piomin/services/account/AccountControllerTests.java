package pl.piomin.services.account;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.testcontainers.consul.ConsulContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.piomin.services.account.model.Account;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class AccountControllerTests {

    @Autowired
    TestRestTemplate restTemplate;

    @Container
    static ConsulContainer consulContainer = new ConsulContainer("consul:1.14")
            .withConsulCommand("kv put config/account-service test=abc");

    @BeforeAll
    static void init() {
        System.setProperty("spring.cloud.consul.port", consulContainer.getFirstMappedPort().toString());
    }

    @Test
    void findAll() {
        List<Long> ids = List.of(1L, 2L, 3L);
        Account[] accounts = restTemplate.postForObject("/ids", ids, Account[].class);
        assertEquals(3, accounts.length);
    }

    @Test
    void findByCustomerId() {
        Account[] accounts = restTemplate.getForObject("/customer/{customerId}", Account[].class, 1L);
        assertTrue(accounts.length > 0);
    }

    @Test
    void add() {
        Account a = new Account("2234567890", 50000, 10L);
        a = restTemplate.postForObject("/", a, Account.class);
        assertNotNull(a);
        assertNotNull(a.getId());
    }

    @Test
    void withdrawOk() {
        ResponseEntity<Account> r = restTemplate.exchange("/withdraw/1/10000", HttpMethod.PUT, null, Account.class);
        assertEquals(200, r.getStatusCodeValue());
        assertNotNull(r.getBody());
        assertEquals(r.getBody().getBalance(), 40000);
    }

    @Test
    void withdrawFailed() {
        ResponseEntity<Account> r = restTemplate.exchange("/withdraw/2/100000", HttpMethod.PUT, null, Account.class);
        assertEquals(500, r.getStatusCodeValue());
    }
}
