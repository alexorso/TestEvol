package org.testevol.engine;

import java.util.List;

import org.testevol.domain.Version;
import org.testevol.engine.domain.TestEvolLog;

public abstract class Task {
	
    protected List<Version> versions;
    protected TestEvolLog log;
    
    public Task(List<Version> versions, TestEvolLog log) {
        this.versions = versions;
        this.log = log;
    }
    
    public abstract boolean go() throws Exception;
}
