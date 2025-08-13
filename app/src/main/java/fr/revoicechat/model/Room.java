package fr.revoicechat.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "RVC_ROOM")
public class Room implements Serializable {
  @Id
  private UUID id;
  private String name;
  @ManyToOne
  @JoinColumn(name="SERVER_ID", nullable=false)
  private Server server;

  public Room() {
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

  public Server getServer() {
    return server;
  }

  public void setServer(final Server server) {
    this.server = server;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) { return true; }
    if (!(o instanceof Room room)) { return false; }
    return Objects.equals(id, room.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
