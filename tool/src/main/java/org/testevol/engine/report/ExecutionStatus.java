package org.testevol.engine.report;

public enum ExecutionStatus {
	
	INITIALIZED {
		@Override
		public String getCode() {
			return "0";
		}

		@Override
		public String getLabel() {
			return "Initialized";
		}

		@Override
		public String getStyle() {
			return "label";
		}
	}, RUNNING {
		@Override
		public String getCode() {
			return "1";
		}
		@Override
		public String getLabel() {
			return "Running";
		}
		@Override
		public String getStyle() {
			return "label label-warning";
		}
	}, SUCCESS {
		@Override
		public String getCode() {
			return "2";
		}
		@Override
		public String getLabel() {
			return "Success";
		}
		@Override
		public String getStyle() {
			return "label label-success";
		}
	}, ERROR {
		@Override
		public String getCode() {
			return "3";
		}
		@Override
		public String getLabel() {
			return "Error";
		}
		@Override
		public String getStyle() {
			return "label label-important";
		}
	};
	
	public abstract String getCode();
	public abstract String getLabel();
	public abstract String getStyle();
	
	public static ExecutionStatus fromCode(String code){
		if("0".equals(code)){
			return INITIALIZED;
		}
		if("1".equals(code)){
			return RUNNING;
		}
		if("2".equals(code)){
			return SUCCESS;
		}
		if("3".equals(code)){
			return ERROR;
		}
		
		return null;
	}

}
