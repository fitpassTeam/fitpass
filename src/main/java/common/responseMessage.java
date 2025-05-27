package common;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class responseMessage<T> {
	private int statusCode;
	private String message;
	private T data;
}
