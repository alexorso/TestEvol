package org.testevol.domain;

import java.io.IOException;
import java.io.StringWriter;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize(include = Inclusion.NON_NULL)
public class VersionSettings {

	public VersionSettings() {
		// TODO Auto-generated constructor stub
	}

	public VersionSettings(String source, String resource, String testSources,
			String testResource, String lib, String javaversion, String basedirOnRepository) {
		super();
		
		this.source = correct(source);
		this.resource = correct(resource);
		this.testSources = correct(testSources);
		this.testResource = correct(testResource);
		this.lib = correct(lib);
		this.javaversion = correct(javaversion);
		this.basedirOnRepository = correct(basedirOnRepository);
	}

	private String correct(String value){
		if(value != null && !value.trim().isEmpty()){
			return value.trim();
		}
		return null;
	}
	
	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public void setBasedir(String basedir) {
		this.basedirOnRepository = basedir;
	}

	private String project;
	private String version;
	private String source;
	private String resource;
	private String testSources;
	private String testResource;
	private String lib;
	private String javaversion;
	private String basedirOnRepository;

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getTestSources() {
		return testSources;
	}

	public void setTestSources(String testSources) {
		this.testSources = testSources;
	}

	public String getTestResource() {
		return testResource;
	}

	public void setTestResource(String testResource) {
		this.testResource = testResource;
	}

	public String getLib() {
		return lib;
	}

	public void setLib(String lib) {
		this.lib = lib;
	}

	public String getJavaversion() {
		return javaversion;
	}

	public void setJavaversion(String javaversion) {
		this.javaversion = javaversion;
	}
	
	public String getBasedirOnRepository() {
		return basedirOnRepository;
	}
	
	public void setBasedirOnRepository(String basedirOnRepository) {
		this.basedirOnRepository = basedirOnRepository;
	}

	@JsonIgnore
	public String getJsonRepresentation() throws JsonGenerationException, JsonMappingException, IOException{
		ObjectMapper objectMapper = new ObjectMapper();
		StringWriter sw = new StringWriter();
		objectMapper.writeValue(sw, this);
		return sw.toString();
	}
	
}
