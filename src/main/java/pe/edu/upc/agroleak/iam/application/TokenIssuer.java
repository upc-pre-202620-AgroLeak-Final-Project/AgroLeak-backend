package pe.edu.upc.agroleak.iam.application;

import pe.edu.upc.agroleak.iam.domain.model.User;

public interface TokenIssuer {
    String issue(User user);
}
