package org.example.fitpass.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.example.fitpass.common.error.SuccessCode;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseMessage<T> {

	private int statusCode;
	private String message;
	private T data;

	public static <T> ResponseMessage<T> success(SuccessCode successCode, T data) {
		return new ResponseMessage<>(
				successCode.getHttpStatus().value(),
				successCode.getMessage(),
				data
		);
	}

	public static <T> ResponseMessage<T> success(SuccessCode successCode) {
		return new ResponseMessage<>(
				successCode.getHttpStatus().value(),
				successCode.getMessage(),
				null
		);
	}
}

