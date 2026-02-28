package app.system.fidelity.core.messaging;

import app.system.fidelity.core.Command;

import java.util.concurrent.CompletableFuture;

public interface SendWelcomeEmailPort extends Command<CompletableFuture<Void>> {
}