package app.system.fidelity.core.business;

import app.system.fidelity.core.Command;
import app.system.fidelity.domain.AppointmentDetail;

import java.util.List;

public interface GetAppointmentDetailsPort extends Command<List<AppointmentDetail>> {
}
