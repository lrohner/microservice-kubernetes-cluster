package ch.valtech.kubernetes.microservice.cluster.filestorage.service;

import ch.valtech.kubernetes.microservice.cluster.persistence.api.dto.Action;
import ch.valtech.kubernetes.microservice.cluster.persistence.api.dto.AuditingRequestDto;
import ch.valtech.kubernetes.microservice.cluster.persistence.api.dto.MessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service("auditingServiceRest")
public class AuditingServiceRest implements AuditingService {

  private final String persistenceUrl;
  private final RestTemplate restTemplate;

  public AuditingServiceRest(@Value("${application.persistence.url}") String persistenceUrl,
      @Qualifier("jwtRestTemplate") RestTemplate restTemplate) {
    this.persistenceUrl = persistenceUrl;
    this.restTemplate = restTemplate;
  }

  @Override
  public MessageDto audit(String filename, Action action) {
    AuditingRequestDto auditingRequest = AuditingRequestDto.builder()
        .filename(filename)
        .action(action).build();
    HttpEntity<AuditingRequestDto> request = new HttpEntity<>(auditingRequest, populateHeaders());
    return postForEntity(request).getBody();
  }

  private HttpHeaders populateHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  public HttpEntity<MessageDto> postForEntity(HttpEntity<AuditingRequestDto> httpRequest) {
    try {
      return restTemplate.postForEntity(persistenceUrl, httpRequest, MessageDto.class);
    } catch (RestClientException e) {
      return handleException(e);
    }
  }

  private ResponseEntity<MessageDto> handleException(RestClientException e) {
    if (e instanceof HttpStatusCodeException) {
      return ResponseEntity.status(((HttpStatusCodeException) e).getStatusCode()).build();
    }

    log.error("Failed to audit", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }

}