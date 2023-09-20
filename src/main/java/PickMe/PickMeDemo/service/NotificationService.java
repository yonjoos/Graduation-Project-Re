package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.NotificationDto;
import PickMe.PickMeDemo.dto.NotificationMessageDto;
import PickMe.PickMeDemo.entity.Notifications;
import PickMe.PickMeDemo.entity.QNotifications;
import PickMe.PickMeDemo.entity.User;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.repository.EmitterRepository;
import PickMe.PickMeDemo.repository.NotificationsRepository;
import PickMe.PickMeDemo.repository.UserRepository;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    // 기본 타임아웃 설정
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    private final EmitterRepository emitterRepository;
    private final UserRepository userRepository;
    private final NotificationsRepository notificationsRepository;
    private final JPAQueryFactory queryFactory;

    /**
     * 클라이언트가 정기적으로 알림을 받기 위해 호출하는 메서드.
     *
     * @param userId - 알림을 받고자 하는 사용자의 pk.
     * @return SseEmitter - 서버에서 보낸 이벤트 Emitter
     */
    public SseEmitter subscribe(Long userId) {
        System.out.println("============start subscribe=========");
        SseEmitter emitter = createEmitter(userId);
        // toString()쓰면, 상대방의 SSE가 안켜졌을 때 NullPointerException 발생함!
        System.out.println("emitter = " + emitter);
        System.out.println("========end subscribe==========");

        NotificationMessageDto notificationMessageDto = new NotificationMessageDto("EventStream Created. [userId=" + userId + "]");

        sendToClient(userId, notificationMessageDto);
        return emitter;
    }

    /**
     * 서버의 이벤트를 클라이언트에게 보내는 메서드
     * 다른 서비스 로직에서 이 메서드를 사용해 데이터를 Object event에 넣고 전송하면 된다.
     *
     * @param userId - 메세지를 전송할 사용자의 아이디.
     * @param event  - 전송할 이벤트 객체.
     */
    public void notify(Long userId, Object event) {
        sendToClient(userId, event);
    }

    /**
     * 클라이언트에게 데이터를 전송
     *
     * @param id   - 데이터를 받을 사용자의 아이디.
     * @param data - 전송할 데이터.
     */
    private void sendToClient(Long id, Object data) {
        System.out.println("==========start send To Client============");
        System.out.println("=======start emitterRepository.get(id)============");
        SseEmitter emitter = emitterRepository.get(id);
        // toString()쓰면, 상대방의 SSE가 안켜졌을 때 NullPointerException 발생함!
        System.out.println("emitter = " + emitter);
        if (emitter != null) {
            try {
                System.out.println("==========================send emitter=================================");
                emitter.send(SseEmitter.event().id(String.valueOf(id)).name("sse").data(data));
                System.out.println("==========================send emitter=================================");
            } catch (IOException exception) {
                emitterRepository.deleteById(id);
                emitter.completeWithError(exception);
            }
        }
        System.out.println("=======end emitterRepository.get(id)============");
        System.out.println("==========end send To Client============");
    }

    /**
     * 사용자 아이디를 기반으로 이벤트 Emitter를 생성
     *
     * @param id - 사용자 아이디.
     * @return SseEmitter - 생성된 이벤트 Emitter.
     */
    private SseEmitter createEmitter(Long id) {
        System.out.println("========start create emmiter===========");
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(id, emitter);

        // Emitter가 완료될 때(모든 데이터가 성공적으로 전송된 상태) Emitter를 삭제한다.
        emitter.onCompletion(() -> emitterRepository.deleteById(id));
        // Emitter가 타임아웃 되었을 때(지정된 시간동안 어떠한 이벤트도 전송되지 않았을 때) Emitter를 삭제한다.
        emitter.onTimeout(() -> emitterRepository.deleteById(id));

        System.out.println("========end create emmiter===========");

        return emitter;
    }

    // 각 회원의 모든 알림 내용을 가져오는 로직
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotifications(String userEmail) {

        QNotifications notifications = QNotifications.notifications;

        // email로 현재 유저 찾기
        User findUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        JPAQuery<Notifications> query = queryFactory.selectFrom(notifications)
                .where(notifications.user.eq(findUser)) // 해당 유저의 알림만 가져올건데
                .orderBy(notifications.createdDate.desc()); // 생성시간 내림차순으로

        //List<Notifications> findNotifications = notificationsRepository.findByUser(findUser); // 모든 알림 내용 가져오기 (List로 변경)
        List<Notifications> findNotifications = query.fetch();
        List<NotificationDto> findNotificationList = new ArrayList<>(); // 모든 알림 엔티티들을 dto 리스트로 변환해서 반환할 것임

        // 만약 해당 회원에게 아무 알림도 없다면, 모든 값을 null로 세팅한 dto 하나 넣은 list반환
        if(findNotifications.isEmpty()) {
            NotificationDto notificationDto = NotificationDto.builder()
                    .notificationId(null)
                    .postId(null)
                    .notificationMessage(null)
                    .postType(null)
                    .isRead(null)
                    .build();

            findNotificationList.add(notificationDto);
        }

        // 만약 해당 회원에게 알림이 있다면, 값을 세팅해서 dto리스트를 만들어서 반환
        else {
            for (Notifications notification : findNotifications) {

                NotificationDto notificationDto = NotificationDto.builder()
                        .notificationId(notification.getId())
                        .postId(notification.getPostId())
                        .notificationMessage(notification.getNotificationMessage())
                        .postType(notification.getPostType().toString()) // 이 방식으로 enum 타입을 문자열로 바꿀 수 있음
                        .isRead(notification.getChecked())
                        .build();

                // 생성된 NotificationDto를 리스트에 추가
                findNotificationList.add(notificationDto);
            }
        }

        // 반환
        return findNotificationList;
    }


    // 알림을 배너에서 삭제하기
    public void deleteNotification(Long notificationId) {

        // 프런트에서 넘어온 알림의 pk값을 기반으로 해당 알림을 찾기
        Notifications findNotification = notificationsRepository.findById(notificationId)
                        .orElseThrow(() -> new AppException("Notification not found", HttpStatus.NOT_FOUND));

        // 찾은 알림을 삭제
        notificationsRepository.delete(findNotification);
    }


    // 알림을 읽음 처리하기
    public void checkNotification(Long notificationId) {

        // 프런트에서 넘어온 알림의 pk값을 기반으로 해당 알림을 찾기
        Notifications findNotification = notificationsRepository.findById(notificationId)
                .orElseThrow(() -> new AppException("Notification not found", HttpStatus.NOT_FOUND));

        // 알림을 읽었다고 표시
        findNotification.setChecked(true);

        // 변경 감지 후 저장
        notificationsRepository.save(findNotification);
    }
}