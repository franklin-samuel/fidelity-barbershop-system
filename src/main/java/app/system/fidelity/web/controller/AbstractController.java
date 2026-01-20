package app.system.fidelity.web.controller;

import app.system.fidelity.web.routes.Page;
import app.system.fidelity.web.routes.RouteBase;

import java.lang.reflect.ParameterizedType;

public class AbstractController<R extends RouteBase> {

    Class<R> route;

    public AbstractController() {
        final ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
        route = (Class<R>) parameterizedType.getActualTypeArguments()[0];
    }

    public String forward(final String page) {
        final String result = Page.route(route).forward(page);
        return result;
    }

    public String redirect(final String page) {
        final String result = Page.route(route).redirect(page);
        return result;
    }
}
