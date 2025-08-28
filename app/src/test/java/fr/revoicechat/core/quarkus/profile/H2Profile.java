package fr.revoicechat.core.quarkus.profile;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class H2Profile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
            "quarkus.security.enabled", "false",
            "quarkus.datasource.db-kind", "h2",
            "quarkus.datasource.jdbc.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            "quarkus.datasource.username", "sa",
            "quarkus.datasource.password", "sa",
            "quarkus.hibernate-orm.database.generation", "drop-and-create"
        );
    }
}
