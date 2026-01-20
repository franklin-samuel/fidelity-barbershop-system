package app.system.fidelity.persistence.mapper;
import app.system.fidelity.domain.User;
import app.system.fidelity.persistence.model.UserEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface UserMapper {

    User map(final UserEntity source);

    UserEntity map(final User source);

}
