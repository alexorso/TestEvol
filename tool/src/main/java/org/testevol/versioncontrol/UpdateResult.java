package org.testevol.versioncontrol;

public class UpdateResult {

	private boolean success;
	private String message;
	
	public UpdateResult(boolean success, String message) {
		super();
		this.success = success;
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	
	
	
	
}
