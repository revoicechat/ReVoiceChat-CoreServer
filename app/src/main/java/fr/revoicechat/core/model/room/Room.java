package fr.revoicechat.core.model.room;

import java.util.UUID;

import fr.revoicechat.core.model.RoomType;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Room {

  @Id
  private UUID id;
  @Column(nullable = false)
  private String name;

  protected Room() {
    super();
  }

  public UUID getId() {
    return id;
  }

  public void setId(final UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public abstract RoomType getType();

  public boolean isVoiceRoom() {
    return getType().isVocal();
  }
}
