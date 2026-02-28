package app.system.fidelity.core.business;

import app.system.fidelity.core.Command;
import app.system.fidelity.domain.AppointmentDetail;
import app.system.fidelity.domain.pagination.PageObject;

public interface GetAppointmentDetailsPagedPort extends Command<PageObject<AppointmentDetail>> {
}