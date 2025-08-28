package fr.revoicechat.core.repository;

import java.util.List;

import fr.revoicechat.core.model.Server;

public interface ServerRepository {
  long count();
  List<Server> findAll();
}
