package org.bpfcaudit.bpfcaudit.api.jsonapi;

import com.toedter.spring.hateoas.jsonapi.JsonApiError;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public class JSONAPIException extends Exception {
    @Getter
    private final JsonApiError jsonApiError;

    public JSONAPIException(HttpStatus httpStatus, String message) {
        this.jsonApiError = new JsonApiError()
                .withStatus(String.valueOf(httpStatus.value()))
                .withTitle(message);
    }
}
