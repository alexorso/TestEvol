package org.testevol.engine.domain.coverage;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class Clazz {

	@XmlAttribute
	public String name;
	
	@XmlElementWrapper(name = "lines")
	@XmlElement(name = "line")
	private List<Line> lines;

	public List<Line> getLines() {
		return lines;
	}

	
	public String getName() {
		return name;
	}
	
}
