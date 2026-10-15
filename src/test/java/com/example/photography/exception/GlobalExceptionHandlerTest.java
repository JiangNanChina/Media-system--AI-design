package com.example.photography.exception;

import com.example.photography.dto.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void oversizedUploadReturnsPayloadTooLarge() {
        ResponseEntity<ApiResponse<Object>> response = handler.handleMaxUploadSizeExceededException(
                new MaxUploadSizeExceededException(510L * 1024 * 1024));

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("上传文件超过服务器允许的大小", response.getBody().getMessage());
    }

    @Test
    void validationErrorsUseGenericMessageWithoutLeaveSpecificPrefix() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "loginRequest");
        bindingResult.addError(new FieldError(
                "loginRequest", "password", "", false, null, null, "密码不能为空"));

        ResponseEntity<ApiResponse<Map<String, String>>> response = handler.handleValidationException(
                new MethodArgumentNotValidException(null, bindingResult));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("参数校验失败: 密码不能为空", response.getBody().getMessage());
    }

    @Test
    void runtimeErrorsKeepBusinessMessageAsBadRequest() {
        ResponseEntity<ApiResponse<Object>> response = handler.handleRuntimeException(
                new RuntimeException("用户名或密码错误"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("用户名或密码错误", response.getBody().getMessage());
    }

    @Test
    void genericErrorsUseSafeServerMessage() {
        ResponseEntity<ApiResponse<Object>> response = handler.handleGenericException(
                new Exception("database password details"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("系统发生未知错误，请联系管理员", response.getBody().getMessage());
    }
}
