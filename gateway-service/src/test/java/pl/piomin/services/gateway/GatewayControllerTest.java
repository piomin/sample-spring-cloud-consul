package pl.piomin.services.gateway;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
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

    @Autowired
    TestRestTemplate template;

    @Container
    static ConsulContainer consulContainer = new ConsulContainer("consul:1.14")
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
            order = template.postForObject("/api/order", order, Order.class);
            if (order.getStatus() != OrderStatus.REJECTED) {
                template.put("/api/order/{id}", null, order.getId());
            }
        } catch (Exception e) {

        }
    }

}
