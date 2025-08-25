package fr.revoicechat.repository.impl;

import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import fr.revoicechat.model.Server;
import fr.revoicechat.repository.ServerRepository;

@ApplicationScoped
public class ServerRepositoryImpl implements ServerRepository {
  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public long count() {
    return entityManager.createQuery("SELECT COUNT(s) FROM Server s", Long.class).getSingleResult();
  }

  @Override
  public List<Server> findAll() {
    return entityManager.createQuery("SELECT s FROM Server s", Server.class).getResultList();
  }

}
