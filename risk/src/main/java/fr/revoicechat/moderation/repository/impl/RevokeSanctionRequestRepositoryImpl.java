package fr.revoicechat.moderation.repository.impl;

import java.util.UUID;
import java.util.stream.Stream;

import fr.revoicechat.moderation.model.SanctionRevocationRequest;
import fr.revoicechat.moderation.model.Sanction;
import fr.revoicechat.moderation.repository.RevokeSanctionRequestRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class RevokeSanctionRequestRepositoryImpl implements RevokeSanctionRequestRepository {

  private static final String SANCTION = "sanction";
  private static final String USER_ID = "userId";

  @PersistenceContext EntityManager entityManager;

  @Override
  public Stream<SanctionRevocationRequest> getBySanction(final Sanction sanction) {
    return entityManager.createQuery("""
                            select rsr
                            from RevokeSanctionRequest rsr
                            where rsr.sanction = :sanction
                            """, SanctionRevocationRequest.class)
                        .setParameter(SANCTION, sanction)
                        .getResultStream();
  }

  @Override
  public Stream<SanctionRevocationRequest> getByUser(final UUID userId) {
    return entityManager.createQuery("""
                            select rsr
                            from RevokeSanctionRequest rsr
                            where rsr.sanction.targetedUser = :userId
                            """, SanctionRevocationRequest.class)
                        .setParameter(USER_ID, userId)
                        .getResultStream();
  }
}
