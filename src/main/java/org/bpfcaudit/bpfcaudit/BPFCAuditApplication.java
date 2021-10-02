package org.bpfcaudit.bpfcaudit;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.toedter.spring.hateoas.jsonapi.JsonApiConfiguration;
import com.toedter.spring.hateoas.jsonapi.JsonApiError;
import com.toedter.spring.hateoas.jsonapi.JsonApiErrors;
import org.bpfcaudit.bpfcaudit.api.jsonapi.JSONAPIException;
import org.bpfcaudit.bpfcaudit.auditor.AuditWorker;
import org.bpfcaudit.bpfcaudit.dal.CaptureRepository;
import org.bpfcaudit.bpfcaudit.model.Capture;
import org.bpfcaudit.bpfcaudit.model.CaptureStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
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
