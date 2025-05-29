package org.example.fitpass.common.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResponseMessage<T> {
	private int statusCode;
	private String message;
	private T data;
}
