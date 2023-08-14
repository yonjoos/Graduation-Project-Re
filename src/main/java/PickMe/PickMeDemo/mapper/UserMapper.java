package PickMe.PickMeDemo.mapper;

import PickMe.PickMeDemo.dto.SignUpDto;
import PickMe.PickMeDemo.dto.UserDto;
import PickMe.PickMeDemo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserDto toUserDto(User user);

    // 동일한 형식이 아니므로, 암호 필드를 무시함. (아마 해시 전과 해시 후?? <- 뇌피셜)
    @Mapping(target = "password", ignore = true)
    User signUpToUser(SignUpDto signUpDto);

}