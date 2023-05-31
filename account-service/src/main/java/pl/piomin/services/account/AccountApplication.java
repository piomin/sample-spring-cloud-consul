package pl.piomin.services.account;

import pl.piomin.services.account.model.Account;
import pl.piomin.services.account.repository.AccountRepository;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@SpringBootApplication
public class AccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class);
    }

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludePayload(true);
        loggingFilter.setIncludeHeaders(true);
        loggingFilter.setMaxPayloadLength(1000);
        loggingFilter.setAfterMessagePrefix("REQ:");
        return loggingFilter;
    }

    @Bean
    AccountRepository repository() {
        AccountRepository repository = new AccountRepository();
        repository.add(new Account("1234567890", 50000, 1L));
        repository.add(new Account("1234567891", 50000, 1L));
        repository.add(new Account("1234567892", 50000, 1L));
        repository.add(new Account("1234567893", 50000, 2L));
        repository.add(new Account("1234567894", 50000, 2L));
        repository.add(new Account("1234567895", 50000, 2L));
        repository.add(new Account("1234567896", 50000, 3L));
        repository.add(new Account("1234567897", 50000, 3L));
        repository.add(new Account("1234567898", 50000, 3L));
        return repository;
    }

}
