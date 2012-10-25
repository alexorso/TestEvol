package org.testevol.engine.domain.coverage;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name="coverage")
@XmlAccessorType(XmlAccessType.FIELD)
public class Coverage {

	@XmlAttribute(name = "line-rate")
	private String lineRate;

	@XmlAttribute(name = "branch-rate")
	private String branchRate;

	@XmlAttribute(name = "lines-valid")
	private Integer linesValid;
	@XmlAttribute(name = "lines-covered")
	private Integer linesCovered;

	@XmlElementWrapper(name = "packages")
	@XmlElement(name = "package")
	private List<Package> packages;

	
	public List<Package> getPackages() {
		return packages;
	}
	
	public String getBranchRate() {
		return branchRate;
	}

	public Integer getLinesValid() {
		return linesValid;
	}

	public Integer getLinesCovered() {
		return linesCovered;
	}

	public Integer getBranchesValid() {
		return branchesValid;
	}

	public Integer getBranchesCovered() {
		return branchesCovered;
	}

	public String getComplexity() {
		return complexity;
	}

	@XmlAttribute(name = "branches-valid")
	private Integer branchesValid;
	@XmlAttribute(name = "branches-covered")
	private Integer branchesCovered;
	@XmlAttribute
	private String complexity;

	public Coverage() {
	}

	public static Coverage getInstance(InputStream stream) throws JAXBException {
		Unmarshaller unmarshaller = JAXBContext.newInstance(Coverage.class).createUnmarshaller();
		unmarshaller.setSchema(null);
		Coverage coverage = (Coverage) unmarshaller.unmarshal(stream);
		coverage.populateCoverageSets();
		return coverage;
	}
	
	@XmlTransient
	public Set<String> coveredLines;
	
	public void populateCoverageSets(){
		coveredLines = new HashSet<String>();
		for(Package pack_age:getPackages()){
			for(Clazz clazz:pack_age.getClasses()){
				String className = clazz.getName();
				for(Line line:clazz.getLines()){
					if(line.getHits() > 0){
						coveredLines.add(className+","+line.getNumber());
					}
				}
			}
		}
	}
	
	public Set<String> getCoveredLines() {
		return coveredLines;
	}

	public String getLineRate() {
		return lineRate;
	}

	public String toString()
	{
		return "Line coverage: " + getLinesCovered() + "/" + getLinesValid()+ ", Branch coverage: " + getBranchesCovered() + "/" + getBranchesValid();
	}	

	
}
