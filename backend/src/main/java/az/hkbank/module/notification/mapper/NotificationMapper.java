package az.hkbank.module.notification.mapper;

import az.hkbank.module.notification.dto.NotificationResponse;
import az.hkbank.module.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Notification entity transformations.
 * Converts between Notification entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper {

    /**
     * Converts Notification entity to NotificationResponse DTO.
     *
     * @param notification the notification entity
     * @return notification response DTO
     */
    @Mapping(source = "read", target = "isRead")
    NotificationResponse toNotificationResponse(Notification notification);
}
