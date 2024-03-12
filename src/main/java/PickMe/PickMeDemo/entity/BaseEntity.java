package PickMe.PickMeDemo.entity;

//모든 엔티티에 들어갈 값타입 클래스(auditing 관련)

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class) //이벤트라는 걸 명시적으로 적어줘야함!!
@MappedSuperclass
@Getter
public class BaseEntity extends BaseTimeEntity {

    @CreatedBy //넣어주기
    @Column(updatable = false) //생성한 사람은 바뀌면 안됨
    private String createdBy; //생성한 사람

    @LastModifiedBy //넣어주기
    private String lastModifiedBy; //수정한 사람


}
