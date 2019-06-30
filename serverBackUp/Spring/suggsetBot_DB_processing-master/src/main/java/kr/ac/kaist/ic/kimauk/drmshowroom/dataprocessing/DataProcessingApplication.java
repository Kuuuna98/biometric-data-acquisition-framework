package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing;

import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.service.CSVImporter;
import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.service.UpdateLog;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DataProcessingApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataProcessingApplication.class, args);
	}

    @Bean
    public CommandLineRunner commandLineRunner(UpdateLog updateLog, CSVImporter csvImporter) {
        return args -> {
            csvImporter.run();
        };
    }
}
