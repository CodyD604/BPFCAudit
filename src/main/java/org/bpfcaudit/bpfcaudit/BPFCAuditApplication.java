package org.bpfcaudit.bpfcaudit;

import org.bpfcaudit.bpfcaudit.auditor.AuditWorker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BPFCAuditApplication {

	public static void main(String[] args) {
		SpringApplication.run(BPFCAuditApplication.class, args);
	}

	@Bean
	public CommandLineRunner bpfcaudit(AuditWorker auditWorker) {
		return (args) -> {
			auditWorker.run();
		};
	}
}
