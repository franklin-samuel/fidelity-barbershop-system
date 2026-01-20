package app.system.fidelity.core;

public interface Command<R> {
    R execute(final Context context);
}