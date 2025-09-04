package fr.revoicechat.core.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "RVC_MESSAGE")
public class Message {
  @Id
  private UUID id;
  @Column(columnDefinition = "TEXT")
  private String text;
  private LocalDateTime createdDate;
  @ManyToOne
  @JoinColumn(name="ROOM_ID", nullable=false)
  private Room room;
  @ManyToOne
  @JoinColumn(name="USER_ID", nullable=false)
  private User user;
  @ManyToMany
  @JoinTable(name = "RVC_MEASSAGE_MEDIA",
      joinColumns = @JoinColumn(name = "MEASSAGE_ID", referencedColumnName = "ID"),
      inverseJoinColumns = @JoinColumn(name = "MEDIA_ID", referencedColumnName = "ID"))
  private List<MediaData> mediaDatas;

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

  public User getUser() {
    return user;
  }

  public void setUser(final User user) {
    this.user = user;
  }

  public List<MediaData> getMediaDatas() {
    if (this.mediaDatas == null) {
      this.mediaDatas = new ArrayList<>();
    }
    return this.mediaDatas;
  }

  public void addMediaData(final MediaData mediaData) {
    if (this.mediaDatas == null) {
      this.mediaDatas = new ArrayList<>();
    }
    mediaDatas.add(mediaData);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) { return true; }
    if (!(o instanceof Message message)) { return false; }
    return Objects.equals(getId(), message.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }
}
