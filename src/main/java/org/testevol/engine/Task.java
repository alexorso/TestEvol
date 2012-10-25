package org.testevol.engine;

import java.util.List;

import org.testevol.domain.Version;
import org.testevol.engine.util.Utils;

public abstract class Task {
	
    protected List<Version> versions;

    
    /**
     * sourceDirectory is the root directory for the subject regexpVersionNames
     * is a regexp that mathes all and only the versions that must be analyzed
     * 
     * Assumptions: (1) No recursive directory structure for versions. (2) All
     * versions either contain a file "javaversion.txt" or will be compiled
     * withot compatibility flags. (3) All versions either contain a file
     * "classpath.txt" or all needed jars in a directory "lib". (4) All source
     * code is under directory "src". (5) Directory "bin" is not used (and will
     * be used to store binaries). (6) Test source code may be included separately
     * under the directory "src-tests". The user may specify a different source fodler
     * using the sysmte propert "src.tests"
     * 
     * @param sourceDirectory
     * @param regexpVersionNames
     */
    public Task(List<Version> versions) {
        this.versions = versions;
    }

    protected abstract String[] getGeneratedFiles();

    public boolean isAlreadyRun() {
//        if (new File(sourceDirectory + File.separator + "."
//                + this.getClass().getName()).exists()) {
//            return true;
//        } else {
//            return false;
//        }
    	return false;
    }

    public void markAsRun() {
//        File mark = new File(sourceDirectory + File.separator + "."
//                + this.getClass().getName());
//        if (mark.exists()) {
//            return;
//        } else {
//            try {
//                mark.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    public void clean() {
//        File mark = new File(sourceDirectory + File.separator + "."
//                + this.getClass().getName());
//        if (mark.exists()) {
//            try {
//                FileUtils.forceDelete(mark);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            Utils.println("Nothing to clean for " + this.getClass().getName());
//        }
    }

    public boolean go() throws Exception {
        return go(false);
    }

    public boolean go(boolean force) throws Exception {
        if (isAlreadyRun() && !force) {
            Utils.println(this.getClass().getName() + " already run");
            return false;
        } else {
            return true;
        }
    }

    public void cleanUp() {
//        for (File verDir : Utils.getMatchingFiles(sourceDirectory, regexpVersionNames)) {
//            for (String filename : getGeneratedFiles()) {
//                for (File file : Utils.getMatchingFiles(verDir, filename)) {
//                    Utils.removeFilesRecursively(file);
//                }
//            }
//        }
//        new File(sourceDirectory + File.separator + "."
//                + this.getClass().getName()).delete();
    }
}
