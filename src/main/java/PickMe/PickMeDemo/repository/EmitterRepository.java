package PickMe.PickMeDemo.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@RequiredArgsConstructor
public class EmitterRepository {
    // 모든 Emitters를 저장하는 ConcurrentHashMap
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * 주어진 아이디와 이미터를 저장
     *
     * @param id      - 사용자 아이디.
     * @param emitter - 이벤트 Emitter.
     */
    public void save(Long id, SseEmitter emitter) {
        System.out.println("========start save===========");
        SseEmitter put = emitters.put(id, emitter);
        System.out.println("put = " + put);
        System.out.println("========end save===========");
    }

    /**
     * 주어진 아이디의 Emitter를 제거
     *
     * @param id - 사용자 아이디.
     */
    public void deleteById(Long id) {
        System.out.println("========start deleteById===========");
        emitters.remove(id);
        System.out.println("========end deleteById===========");
    }

    /**
     * 주어진 아이디의 Emitter를 가져옴.
     *
     * @param id - 사용자 아이디.
     * @return SseEmitter - 이벤트 Emitter.
     */
    public SseEmitter get(Long id) {
        System.out.println("========start get emitters===========");
        return emitters.get(id);
    }
}