package fr.revoicechat.core.repository.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import fr.revoicechat.core.model.Room;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import fr.revoicechat.core.model.Message;
import fr.revoicechat.core.repository.MessageRepository;
import fr.revoicechat.core.repository.page.PageResult;

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

  @Override
  public Stream<Message> findByRoom(Room room) {
    return em.createQuery("""
                 SELECT m
                 FROM Message m
                 WHERE m.room = :room
                 ORDER BY m.createdDate DESC""", Message.class)
             .setParameter("room", room)
             .getResultStream();
  }

  @Override
  public Message findByMedia(final UUID mediaId) {
    return em.createQuery("""
                 select m
                 from Message m
                 join m.mediaDatas md
                 where md.id = :id""", Message.class)
             .setParameter("id", mediaId)
             .getSingleResult();
  }

}
