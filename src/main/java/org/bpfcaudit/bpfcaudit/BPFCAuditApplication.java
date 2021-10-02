package org.bpfcaudit.bpfcaudit;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.toedter.spring.hateoas.jsonapi.JsonApiConfiguration;
import com.toedter.spring.hateoas.jsonapi.JsonApiError;
import com.toedter.spring.hateoas.jsonapi.JsonApiErrors;
import org.bpfcaudit.bpfcaudit.api.jsonapi.JSONAPIException;
import org.bpfcaudit.bpfcaudit.auditor.AuditWorker;
import org.bpfcaudit.bpfcaudit.dal.AuditRepository;
import org.bpfcaudit.bpfcaudit.model.Audit;
import org.bpfcaudit.bpfcaudit.model.AuditStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
	public CommandLineRunner bpfcaudit(AuditRepository auditRepository) throws ExecutionException, InterruptedException {
		// TODO: Should this block? Does this run before APIs can be hit?
		return (args) -> {
			// Clean up dangling audits
			CompletableFuture<List<Audit>> auditFuture = auditRepository.findByStatus(AuditStatus.IN_PROGRESS);

			auditFuture.handle((List<Audit> audits, Throwable t) -> {
				for (Audit audit : audits) {
					audit.setFailure("Process terminated before audit completion.");
				}
				auditRepository.saveAll(audits);
				return null;
			});
		};
	}

	@ControllerAdvice
	public class JSONAPIExceptionHandler {
		@ExceptionHandler(value={JSONAPIException.class})
		public ResponseEntity<JsonApiErrors> handleJSONAPIException(JSONAPIException jsonapiException) {
			JsonApiError jsonApiError = jsonapiException.getJsonApiError();

			return ResponseEntity
					.status(HttpStatus.valueOf(Integer.parseInt(jsonApiError.getStatus())))
					.body(new JsonApiErrors(jsonApiError));
		}

		@ExceptionHandler(value={Exception.class})
		public ResponseEntity<JsonApiErrors> handleException(Exception ex) {
			JsonApiError jsonApiError = new JsonApiError()
					.withStatus(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
					.withTitle(ex.getMessage());

			return ResponseEntity
					.internalServerError()
					.body(new JsonApiErrors().withError(jsonApiError));
		}
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
