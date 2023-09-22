package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.entity.Notifications;
import PickMe.PickMeDemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationsRepository extends JpaRepository<Notifications,Long> {

    List<Notifications> findByUser(User user);

    // 해당하는 유저의 알림 중, 읽은 알림을 모두 찾기
    List<Notifications> findByUserAndChecked(User user, Boolean checked);
}
