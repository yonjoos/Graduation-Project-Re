package PickMe.PickMeDemo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

//연주 첫 주석!
//시홍 첫 주석!
@SpringBootApplication
@EnableJpaAuditing //이게 있어야 datajpa에서 수정일자, 생성일자, 생성자, 수정자 관리할 수 있음!!!!!!!
public class PickMeDemoApplication {

	public static void main(String[] args) {

		SpringApplication.run(PickMeDemoApplication.class, args);


	}

	@Bean //생성한 사람과 수정한 사람을 세팅하는 걸 도와주는 빈
	//세션 정보나, 스프링 시큐리티 로그인 정보에서 ID에서 꺼낸다. 현재는 랜덤함수로 돌아감, 추후에 다시 고쳐볼 것!
	public AuditorAware<String> auditorProvider(){
		return () -> Optional.of(UUID.randomUUID().toString());
	}



}
