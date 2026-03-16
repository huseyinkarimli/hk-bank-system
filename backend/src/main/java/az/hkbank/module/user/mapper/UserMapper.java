package az.hkbank.module.user.mapper;

import az.hkbank.module.user.dto.RegisterRequest;
import az.hkbank.module.user.dto.UserResponse;
import az.hkbank.module.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for User entity conversions.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Converts User entity to UserResponse DTO.
     *
     * @param user the user entity
     * @return UserResponse DTO
     */
    UserResponse toUserResponse(User user);

    /**
     * Converts RegisterRequest DTO to User entity.
     * Note: Password, role and other fields must be set manually in service layer.
     *
     * @param request the registration request
     * @return User entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toUser(RegisterRequest request);
}