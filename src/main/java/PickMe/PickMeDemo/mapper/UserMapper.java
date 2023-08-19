package PickMe.PickMeDemo.mapper;

import PickMe.PickMeDemo.dto.SignUpDto;
import PickMe.PickMeDemo.dto.UserDto;
import PickMe.PickMeDemo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * @Mapper 어노테이션은 MapStruct 라이브러리의 사용을 선언하며,
 * componentModel = "spring"으로 Spring 컴포넌트 모델을 사용하고,
 * unmappedTargetPolicy = ReportingPolicy.IGNORE으로 매핑되지 않은 필드에 대한 경고를 무시.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    // toUserDto(User user) 메서드는 엔티티 User를 DTO UserDto로 변환함.
    UserDto toUserDto(User user);

    // 동일한 형식이 아니므로, 암호 필드를 무시함.
    // signUpToUser(SignUpDto signUpDto) 메서드는 회원가입을 위한 DTO SignUpDto를 엔티티 User로 변환하며, 비밀번호 필드는 무시됨.
    @Mapping(target = "password", ignore = true)
    User signUpToUser(SignUpDto signUpDto);

}