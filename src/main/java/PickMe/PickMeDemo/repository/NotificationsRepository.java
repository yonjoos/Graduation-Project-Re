package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.entity.Notifications;
import PickMe.PickMeDemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationsRepository extends JpaRepository<Notifications,Long> {

    List<Notifications> findByUser(User user);

}
