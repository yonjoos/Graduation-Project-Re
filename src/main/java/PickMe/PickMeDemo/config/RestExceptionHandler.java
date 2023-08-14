package PickMe.PickMeDemo.config;

import PickMe.PickMeDemo.dto.ErrorDto;
import PickMe.PickMeDemo.exception.AppException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class RestExceptionHandler {

    // 이 방법은 모든 Controller를 둘러 쌈.
    @ExceptionHandler(value = { AppException.class })   // 그러나 exception 패키지의 AppException이 발생한 경우, 예외가 발생한 경우에만 실행됨.
    @ResponseBody
    public ResponseEntity<ErrorDto> handleException(AppException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(ErrorDto.builder().message(ex.getMessage()).build());
    }
}