package app.system.fidelity.web.routes;

public interface RouteBase {

    String FORM = "/form";

    String LIST = "/list";

    String VIEW = "/view";

    String EDIT = "/edit/{id}";

    String DELETE = "/delete/{id}";

    String NOT_FOUND = "/error/404";

    String ERROR = "/error/503";

    String PERMISSION_DENIED = "/error/403";

    String INDEX = "/index";

    String CANCEL = "/cancel/{id}";

}

