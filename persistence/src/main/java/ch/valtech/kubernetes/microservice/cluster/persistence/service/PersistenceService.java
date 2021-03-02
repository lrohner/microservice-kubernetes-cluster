package ch.valtech.kubernetes.microservice.cluster.persistence.service;

import ch.valtech.kubernetes.microservice.cluster.persistence.domain.Auditing;
import ch.valtech.kubernetes.microservice.cluster.persistence.domain.Message;
import ch.valtech.kubernetes.microservice.cluster.persistence.domain.User;
import ch.valtech.kubernetes.microservice.cluster.persistence.dto.AuditingRequestDTO;
import ch.valtech.kubernetes.microservice.cluster.persistence.dto.MessageDTO;
import ch.valtech.kubernetes.microservice.cluster.persistence.exception.PersistenceException;
import ch.valtech.kubernetes.microservice.cluster.persistence.repository.AuditingRepository;
import ch.valtech.kubernetes.microservice.cluster.persistence.repository.MessageRepository;
import ch.valtech.kubernetes.microservice.cluster.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;

@Service
@Transactional
public class PersistenceService {
  
  private final MessageRepository messageRepository;
  private final AuditingRepository auditingRepository;
  private final UserRepository userRepository;

  public PersistenceService(MessageRepository messageRepository,
      AuditingRepository auditingRepository,
      UserRepository userRepository) {
    this.messageRepository = messageRepository;
    this.auditingRepository = auditingRepository;
    this.userRepository = userRepository;
  }
  
  private Message getMessageByKey(String key) {
    
    Optional<Message> message = messageRepository.findByKeyId(key);
    if (message.isEmpty()) {
      throw new PersistenceException("Message is not found");
    }
    return message.get();
  }
  
  public MessageDTO saveNewMessage(AuditingRequestDTO requestDTO) {
    addAuditRecord(requestDTO);
    
    Message message = getMessageByKey(requestDTO.getKey());
    
    return MessageDTO.builder().key(message.getKeyId()).message(message.getValue()).build();
  }
  
  private void addAuditRecord(AuditingRequestDTO requestDTO) {
    Auditing auditing = new Auditing();
    auditing.setUser(getOrCreateUser(requestDTO.getEmail()));
    auditing.setMessage(new Message(requestDTO.getKey(), requestDTO.getMessageValue()));
    auditing.setModificationDate(LocalDate.now());
    
    auditingRepository.saveAndFlush(auditing);
  }

  private User getOrCreateUser(String email) {
    return userRepository.findByEmail(email).orElse(new User(email));
  }

}
