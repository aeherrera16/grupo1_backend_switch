package ec.edu.espe.banquito.emailservice.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Client for communicating with the main Switch API.
 */
@Component
public class SwitchApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(SwitchApiClient.class);

    private final RestTemplate restTemplate;

    private final String baseUrl;
    private final String endpoint;

    public SwitchApiClient(
            @org.springframework.beans.factory.annotation.Value("${switch.api.base-url}") String baseUrl,
            @org.springframework.beans.factory.annotation.Value("${switch.api.endpoint}") String endpoint,
            @org.springframework.beans.factory.annotation.Value("${switch.api.timeout}") int timeout) {
        this.baseUrl = baseUrl;
        this.endpoint = endpoint;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    /**
     * Sends a CSV file to the main switch through the REST API
     */
    public boolean sendFileToSwitch(File file) {
        try {
            String url = baseUrl + endpoint;
            LOG.info("Connecting to the main Switch");
            LOG.info("Sending file {} to switch: {}", file.getName(), url);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            Resource fileResource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return file.getName();
                }
            };
            
            body.add("file", fileResource);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                LOG.info("Connected to the main Switch successfully");
                LOG.info("File {} sent successfully. Response: {}", file.getName(), response.getBody());
                return true;
            } else {
                LOG.error("Error sending file to the Switch. Status: {}", response.getStatusCode());
                return false;
            }
            
        } catch (IOException e) {
            LOG.error("Error reading file {}: {}", file.getName(), e.getMessage());
            return false;
        } catch (Exception e) {
            LOG.error("Error connecting to the main Switch: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks whether the switch is available
     */
    public boolean isSwitchAvailable() {
        try {
            String url = baseUrl + "/switch/v1/payment-batch";
            LOG.debug("Checking main Switch availability: {}", url);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                LOG.debug("Main Switch available");
            }
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            LOG.warn("Main Switch unavailable: {}", e.getMessage());
            return false;
        }
    }
}
