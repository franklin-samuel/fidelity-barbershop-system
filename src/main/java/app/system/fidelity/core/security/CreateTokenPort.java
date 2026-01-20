package app.system.fidelity.core.security;

import app.system.fidelity.core.Command;
import app.system.fidelity.domain.Jwt;

public interface CreateTokenPort extends Command<Jwt> {
}
