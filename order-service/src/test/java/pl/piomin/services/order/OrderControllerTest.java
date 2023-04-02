package pl.piomin.services.order;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.testcontainers.consul.ConsulContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.piomin.services.order.model.Order;
import pl.piomin.services.order.model.OrderStatus;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Testcontainers
public class OrderControllerTest {

//	@Autowired
	TestRestTemplate template;

//	@Container
	static ConsulContainer consulContainer = new ConsulContainer("consul:1.14")
			.withConsulCommand("kv put config/customer-service test=abc");

//	@BeforeAll
	static void init() {
		System.setProperty("spring.cloud.consul.port", consulContainer.getFirstMappedPort().toString());
		System.setProperty("spring.config.import", "optional:consul:localhost:" + consulContainer.getFirstMappedPort());
	}

//	@Test
	public void testOrder() throws InterruptedException {
		sendAndAcceptOrder();
	}

	private void sendAndAcceptOrder() {
		Random r = new Random();
		Order order = new Order();
		order.setCustomerId((long) r.nextInt(3) + 1);
		order.setProductIds(Arrays.asList(new Long[] { (long) r.nextInt(10) + 1, (long) r.nextInt(10) + 1 }));
		order = template.postForObject("/", order, Order.class);
		assertNotNull(order);
		assertNotNull(order.getId());
		assertNotSame(order.getStatus(), OrderStatus.REJECTED);

		template.put("/{id}", null, order.getId());
	}
	
}
