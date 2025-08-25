package fr.revoicechat.repository.impl;

import java.util.List;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import fr.revoicechat.model.Message;
import fr.revoicechat.repository.MessageRepository;
import fr.revoicechat.repository.page.PageResult;

@ApplicationScoped
public class MessageRepositoryImpl implements MessageRepository {

  @PersistenceContext
  private EntityManager em;

  @Override
  public PageResult<Message> findByRoomId(UUID roomId, int page, int size) {
    // Fetch content
    List<Message> content = em.createQuery("""
                                               SELECT m
                                               FROM Message m
                                               WHERE m.room.id = :roomId
                                               ORDER BY m.createdDate DESC""", Message.class)
                              .setParameter("roomId", roomId)
                              .setFirstResult(page * size)   // offset
                              .setMaxResults(size)           // limit
                              .getResultList();
    // Count total
    Long total = em.createQuery(
                       "SELECT COUNT(m) FROM Message m WHERE m.room.id = :roomId",
                       Long.class)
                   .setParameter("roomId", roomId)
                   .getSingleResult();

    return new PageResult<>(content, page, size, total);
  }

}
