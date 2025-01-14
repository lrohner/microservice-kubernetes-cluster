package ch.valtech.kubernetes.microservice.cluster.persistence.api.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class MessageDto {

  String message;

}
