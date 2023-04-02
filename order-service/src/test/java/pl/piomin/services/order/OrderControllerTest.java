package pl.piomin.services.order;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit5.HoverflyExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.testcontainers.consul.ConsulContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.piomin.services.order.model.Order;
import pl.piomin.services.order.model.OrderStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ExtendWith(HoverflyExtension.class)
public class OrderControllerTest {

	@Autowired
	TestRestTemplate template;

	@Container
	static ConsulContainer consulContainer = new ConsulContainer("consul:1.14")
			.withConsulCommand("kv put config/order-service test=abc");

	@BeforeAll
	static void init() {
		System.setProperty("spring.cloud.consul.port", consulContainer.getFirstMappedPort().toString());
		System.setProperty("spring.config.import", "optional:consul:localhost:" + consulContainer.getFirstMappedPort());
	}

	@Test
	public void testOrder(Hoverfly hoverfly) throws InterruptedException {
		hoverfly.simulate(
				dsl(service("http://customer-service")
						.get("/withAccounts/1")
						.willReturn(success().body(
									  	"""
										{
										  "id":1,
										  "type":"REGULAR",
										  "accounts":[{
										    "id":1,
										    "balance":10000
										  }]
										}
										"""
								).header("Content-Type", "application/json"))),
				dsl(service("http://product-service")
						.post("/ids").body("[1]")
						.willReturn(success().body(
										"""
                                        [{
                                          "id":1,
                                          "price":1000,
                                          "name":"Test1"
                                        }]
                                        """
								).header("Content-Type", "application/json")))
		);

		sendAndAcceptOrder();
	}

	private void sendAndAcceptOrder() {
		Random r = new Random();
		Order order = new Order();
		order.setCustomerId(1L);
		order.setProductIds(List.of(1L));
		order = template.postForObject("/", order, Order.class);
		assertNotNull(order);
		assertNotNull(order.getId());
		assertNotSame(order.getStatus(), OrderStatus.REJECTED);

		template.put("/{id}", null, order.getId());
	}
	
}
