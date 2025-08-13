package fr.revoicechat.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "RVC_MESSAGE")
public class Message implements Serializable {
  @Id
  private UUID id;
  private String text;
  private LocalDateTime createdDate;
  @ManyToOne
  @JoinColumn(name="ROOM_ID", nullable=false)
  private Room room;

  public Message() {
    super();
  }

  public UUID getId() {
    return id;
  }

  public void setId(final UUID id) {
    this.id = id;
  }

  public String getText() {
    return text;
  }

  public void setText(final String text) {
    this.text = text;
  }

  public LocalDateTime getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(final LocalDateTime createdDate) {
    this.createdDate = createdDate;
  }

  public Room getRoom() {
    return room;
  }

  public void setRoom(final Room room) {
    this.room = room;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) { return true; }
    if (!(o instanceof Message message)) { return false; }
    return Objects.equals(id, message.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
