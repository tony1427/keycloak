package org.keycloak.vault;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Creates and configures {@link PlainTextVaultProvider}.
 *
 * @author Sebastian Łaskawiec
 */
public class PlainTextVaultProviderFactory implements VaultProviderFactory {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    public static final String PROVIDER_ID = "plaintext";

    private String vaultDirectory;
    private boolean disabled;
    private Path vaultPath;

    @Override
    public VaultProvider create(KeycloakSession session) {
        if (disabled || vaultDirectory == null) {
            //init method not called?
            throw new IllegalStateException("Can not create a vault since it's disabled or not initialized correctly");
        }
        return new PlainTextVaultProvider(vaultPath, session.getContext().getRealm().getName());
    }

    @Override
    public void init(Config.Scope config) {
        vaultDirectory = config.get("dir");

        Boolean disabledFromConfig = config.getBoolean("disabled");
        if (disabledFromConfig == null) {
            disabled = false;
        } else {
            disabled = disabledFromConfig.booleanValue();
        }

        if (disabled) {
            logger.debug("PlainTextVaultProviderFactory disabled");
            return;
        }

        if (vaultDirectory == null) {
            logger.debug("PlainTextVaultProviderFactory not configured");
            return;
        }

        vaultPath = Paths.get(vaultDirectory);
        if (!Files.exists(vaultPath)) {
            throw new VaultNotFoundException("The " + vaultPath.toAbsolutePath().toString() + " directory doesn't exist");
        }
        logger.debugf("Configured PlainTextVaultProviderFactory with directory %s", vaultPath.toString());
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
