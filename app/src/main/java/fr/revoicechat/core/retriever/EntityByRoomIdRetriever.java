package fr.revoicechat.core.retriever;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import fr.revoicechat.core.model.Room;
import fr.revoicechat.risk.RisksEntityRetriever;
import fr.revoicechat.risk.technicaldata.RiskEntity;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.EntityManager;

public class EntityByRoomIdRetriever implements RisksEntityRetriever {

  @Override
  public RiskEntity get(final Method method, final List<DataParameter> parameters) {
    var em = CDI.current().select(EntityManager.class).get();
    return parameters.stream()
                     .map(DataParameter::arg)
                     .filter(UUID.class::isInstance)
                     .findFirst()
                     .map(id -> em.getReference(Room.class, id))
                     .map(room -> new RiskEntity(room.getServer().getId(), room.getId()))
                     .orElse(RiskEntity.EMPTY);
  }
}
