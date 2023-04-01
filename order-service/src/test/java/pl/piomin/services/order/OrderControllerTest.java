package pl.piomin.services.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import pl.piomin.services.order.model.Order;
import pl.piomin.services.order.model.OrderStatus;

import java.util.Arrays;
import java.util.Random;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderControllerTest {

	@Autowired
	TestRestTemplate template;

	@Test
	public void testOrder() throws InterruptedException {
		for (int i = 0; i < 10; i++) {
			sendAndAcceptOrder();
			Thread.sleep(5000);
		}
	}

	private void sendAndAcceptOrder() {
		try {
			Random r = new Random();
			Order order = new Order();
			order.setCustomerId((long) r.nextInt(3) + 1);
			order.setProductIds(Arrays.asList(new Long[] { (long) r.nextInt(10) + 1, (long) r.nextInt(10) + 1 }));
			order = template.postForObject("/", order, Order.class);
			if (order.getStatus() != OrderStatus.REJECTED) {
				template.put("/{id}", null, order.getId());
			}
		} catch (Exception e) {

		}
	}
	
}
