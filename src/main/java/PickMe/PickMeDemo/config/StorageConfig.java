package PickMe.PickMeDemo.config;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

// 구글 클라우드 관련 빈 등록
@Configuration
public class StorageConfig {

    @Bean
    public Storage storage() throws IOException {

        ClassPathResource resource = new ClassPathResource("focal-cipher-402404-669125967bfe.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream());
        String projectId = "focal-cipher-402404";
        return StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();
    }
}
