package app.system.fidelity.web.routes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Page {

    private final StringBuilder path;

    public Page(final String path) {
        this.path = new StringBuilder(path);
    }

    public static Page route(final Class<? extends RouteBase> routeClass) {
        Object value = null;
        try {
            final var field = routeClass.getDeclaredField("ROOT");
            final Class<?> fieldType = field.getType();
            final Object fieldValue = fieldType.getDeclaredConstructor().newInstance();
            value = field.get(fieldValue);
        } catch (InstantiationException | IllegalAccessException | NoSuchFieldException | java.lang.reflect.InvocationTargetException | NoSuchMethodException e) {
            log.debug("Falha ao localizar a rota ROOT.", e);
        }
        return new Page((String) value);
    }

    public String forward(final String page) {
        return this.path.toString() + page;
    }

    public String redirect(final String page) {
        return "redirect:" + this.path.toString() + page;
    }

    public String redirect() {
        return "redirect:" + this.path;
    }

}
