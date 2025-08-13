package fr.revoicechat.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "RVC_SERVER_USER")
public class UserRole {
  @EmbeddedId
  private UserRolePK pk;


  @Embeddable
  public static final class UserRolePK {
    @OneToOne
    @JoinColumn(name="SERVER_ID", nullable=false, updatable=false)
    private Server server;
    @OneToOne
    @JoinColumn(name="USER_ID", nullable=false, updatable=false)
    private User user;

    public Server getServer() {
      return server;
    }

    public void setServer(final Server server) {
      this.server = server;
    }

    public User getUser() {
      return user;
    }

    public void setUser(final User user) {
      this.user = user;
    }
  }
}
