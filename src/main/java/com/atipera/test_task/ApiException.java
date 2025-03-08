package com.atipera.test_task;

public class ApiException extends Throwable {
	private final int status;
	private final String message;

	public ApiException(int status, String message) {
		this.status = status;
		this.message = message;
	}

	public int getStatus() {
		return status;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
