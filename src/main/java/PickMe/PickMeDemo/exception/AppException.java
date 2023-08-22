package PickMe.PickMeDemo.exception;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {

    private final HttpStatus status;

    // 예외에 따라 원하는 HTTP 코드를 반환할 수 있도록 사용자 지정 예외를 만들었음.
    // 이를 위해 사용자 지정 HTTP 코드를 사용하여 내 사용자 지정 메시지를 반환하려면 예외 처리기(config 패키지의 RestExceptionHandler 클래스)가 필요.
    public AppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}