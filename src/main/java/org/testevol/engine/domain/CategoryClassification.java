package org.testevol.engine.domain;

public enum CategoryClassification {
	TESTREP {
		@Override
		public String getLabel() {
			// TODO Auto-generated method stub
			return "TESTREP";
		}
	},

	TESTMODNOTREP {
		@Override
		public String getLabel() {
			// TODO Auto-generated method stub
			return "TESTMODNOTREP";
		}
	},

	TESTDEL_AE_RE {
		@Override
		public String getLabel() {
			// TODO Auto-generated method stub
			return "TESTDEL (AE|RE)";
		}
	},
	TESTDEL_CE {
		@Override
		public String getLabel() {
			// TODO Auto-generated method stub
			return "TESTDEL (CE)";
		}
	},
	TESTDEL_P {
		@Override
		public String getLabel() {
			// TODO Auto-generated method stub
			return "TESTDEL (P)";
		}
	},
	TESTADD_AE_RE {
		@Override
		public String getLabel() {
			// TODO Auto-generated method stub
			return "TESTADD (AE|RE)";
		}
	},
	TESTADD_CE {
		@Override
		public String getLabel() {
			// TODO Auto-generated method stub
			return "TESTADD (CE)";
		}
	},
	TESTADD_P {
		@Override
		public String getLabel() {
			// TODO Auto-generated method stub
			return "TESTADD (P)";
		}
	};

	public abstract String getLabel();

}