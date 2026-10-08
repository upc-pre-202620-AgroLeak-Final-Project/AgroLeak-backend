package pe.edu.upc.agroleak.iam.application;

import java.util.UUID;

public interface CurrentUser {
    UUID id();

    boolean isAdmin();

    boolean isTechnician();
}
