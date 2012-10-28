package org.testevol.engine.domain.coverage;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Classes {
	
	@XmlElement(name="class")
	private List<Clazz> classes;

	
	public List<Clazz> getClasses() {
		return classes;
	}
}
