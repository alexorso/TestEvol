package org.testevol.engine.domain;

import org.testevol.domain.Version;

public class VersionPair {

	private Version oldVersion;
	private Version version;
	
	public VersionPair(Version oldVersion, Version version) {
		super();
		this.oldVersion = oldVersion;
		this.version = version;
	}
	
	public Version getOldVersion() {
		return oldVersion;
	}
	
	public Version getVersion() {
		return version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((oldVersion == null) ? 0 : oldVersion.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VersionPair other = (VersionPair) obj;
		if (oldVersion == null) {
			if (other.oldVersion != null)
				return false;
		} else if (!oldVersion.equals(other.oldVersion))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
	
	
	
	
	
}
