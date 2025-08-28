package fr.revoicechat.core.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import fr.revoicechat.notification.model.NotificationRegistrable;

@Entity
@Table(name = "RVC_USER")
public class User implements Serializable, NotificationRegistrable {
  @Id
  private UUID id;

  @Column(unique = true)
  private String email;
  @Column(unique = true, nullable = false)
  private String login;
  @Column(nullable = false)
  private String displayName;
  private String password;
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private ActiveStatus status = ActiveStatus.ONLINE;
  private LocalDateTime createdDate;
  @Enumerated(EnumType.STRING)
  private UserType type = UserType.USER;

  public User() {
    super();
  }

  @Override
  public UUID getId() {
    return id;
  }

  public void setId(final UUID id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(final String login) {
    this.login = login;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(final String username) {
    this.displayName = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public LocalDateTime getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(final LocalDateTime createdDate) {
    this.createdDate = createdDate;
  }

  public ActiveStatus getStatus() {
    return status;
  }

  public void setStatus(final ActiveStatus status) {
    this.status = status;
  }

  public UserType getType() {
    return type;
  }

  public void setType(final UserType type) {
    this.type = type;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof User user)) {
      return false;
    }
    return Objects.equals(getId(), user.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }
}
