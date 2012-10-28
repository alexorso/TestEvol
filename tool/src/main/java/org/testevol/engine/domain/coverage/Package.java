package org.testevol.engine.domain.coverage;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class Package {

	@XmlAttribute
	String name;
	
	@XmlElementWrapper(name = "classes")
	@XmlElement(name = "class")
	private List<Clazz> classes;

	public List<Clazz> getClasses() {
		return classes;
	}
	
	public String getName() {
		return name;
	}

}
