package pl.piomin.services.gateway;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.consul.ConsulContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.piomin.services.gateway.model.Order;
import pl.piomin.services.gateway.model.OrderStatus;

import java.util.Arrays;
import java.util.Random;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class GatewayControllerTest {

    RestTestClient restClient;

    public GatewayControllerTest(WebApplicationContext context) {
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
    public void testOrder() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            sendAndAcceptOrder();
        }
    }

    private void sendAndAcceptOrder() {
        try {
            Random r = new Random();
            Order order = new Order();
            order.setCustomerId((long) r.nextInt(3) + 1);
            order.setProductIds(Arrays.asList(new Long[]{(long) r.nextInt(10) + 1, (long) r.nextInt(10) + 1}));
            order = restClient.post().uri("/api/order")
                    .body(order)
                    .exchange()
                    .returnResult(Order.class)
                    .getResponseBody();
            if (order.getStatus() != OrderStatus.REJECTED) {
                restClient.put().uri("/api/order/{id}", order.getId())
                        .exchange()
                        .expectStatus().is2xxSuccessful();
            }
        } catch (Exception e) {

        }
    }

}
