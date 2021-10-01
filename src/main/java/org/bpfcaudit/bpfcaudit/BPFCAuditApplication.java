package org.bpfcaudit.bpfcaudit;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.toedter.spring.hateoas.jsonapi.JsonApiConfiguration;
import org.bpfcaudit.bpfcaudit.auditor.AuditWorker;
import org.bpfcaudit.bpfcaudit.dal.CaptureRepository;
import org.bpfcaudit.bpfcaudit.model.Capture;
import org.bpfcaudit.bpfcaudit.model.CaptureStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
public class BPFCAuditApplication implements WebMvcConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(BPFCAuditApplication.class, args);
	}

	@Bean
	public CommandLineRunner bpfcaudit(AuditWorker auditWorker, CaptureRepository captureRepository) throws ExecutionException, InterruptedException {
		// TODO: Should this block? Does this run before APIs can be hit?
		// Clean up dangling captures
		CompletableFuture<List<Capture>> captureFuture = captureRepository.findByStatus(CaptureStatus.IN_PROGRESS);

		captureFuture.handle((List<Capture> captures, Throwable t) -> {
			for (Capture capture: captures) {
				capture.setFailure("Process terminated before capture completion.");
			}
			captureRepository.saveAll(captures);
			return null;
		});

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
