package fr.revoicechat.repository;

import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import fr.revoicechat.model.Message;

public interface MessageRepository extends JpaRepository<Message, UUID> {
  Stream<Message> findByRoomId(UUID id);

  Page<Message> findByRoomId(UUID roomId, Pageable pageable);

}
