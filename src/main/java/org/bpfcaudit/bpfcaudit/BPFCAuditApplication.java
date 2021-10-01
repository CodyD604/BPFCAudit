package org.bpfcaudit.bpfcaudit;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.toedter.spring.hateoas.jsonapi.JsonApiConfiguration;
import org.bpfcaudit.bpfcaudit.auditor.AuditWorker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class BPFCAuditApplication implements WebMvcConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(BPFCAuditApplication.class, args);
	}

	@Bean
	public CommandLineRunner bpfcaudit(AuditWorker auditWorker) {
		// TODO: delete captures with status IN_PROGRESS
		return (args) -> {
			auditWorker.run();
		};
	}

	@Bean
	public JsonApiConfiguration jsonApiConfiguration() {
		return new JsonApiConfiguration()
				.withJsonApiVersionRendered(true)
				.withObjectMapperCustomizer(objectMapper -> {
					// put your additional object mapper config here
					objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
				});
	}
}
