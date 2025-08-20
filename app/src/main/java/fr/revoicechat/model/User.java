package fr.revoicechat.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "RVC_USER")
public class User implements Serializable {
  @Id
  private UUID id;

  @Column(unique = true)
  private String email;
  @Column(unique = true, nullable = false)
  private String login;
  @Column(nullable = false)
  private String displayName;
  private String password;
  @ManyToMany
  @JoinTable(name = "RVC_SERVER_USER",
      joinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "ID"),
      inverseJoinColumns = @JoinColumn(name = "SERVER_ID", referencedColumnName = "ID"))
  private List<Server> servers;

  private LocalDateTime createdDate;

  public User() {
    super();
  }

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

  public List<Server> getServers() {
    return servers;
  }

  public void setServers(final List<Server> servers) {
    this.servers = servers;
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
