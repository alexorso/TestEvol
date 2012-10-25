package org.testevol.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.testevol.domain.Project;
import org.testevol.domain.ProjectRepository;
import org.testevol.domain.Version;
import org.testevol.engine.DataAnalysis;
import org.testevol.versioncontrol.UpdateResult;
import org.testevol.versioncontrol.VersionControlSystem;

@Controller
@RequestMapping("/projects")
public class ProjectController {
	
	@Autowired
	private ProjectRepository projectRepo;

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView init() {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("projects");
		return mav;
	}

	@RequestMapping(value="list",method = RequestMethod.GET)
	public ModelAndView list() throws Exception {
		List<Project> projects = projectRepo.getProjects();
		
		ModelAndView mav = new ModelAndView();
		mav.addObject("projects", projects);
		mav.setViewName("projectList");
		return mav;
	}

	
	@RequestMapping(value="{project}",method = RequestMethod.GET)
	public ModelAndView getProject(@PathVariable("project") String projectName) throws Exception {
		Project project = projectRepo.getProject(projectName);
		
		ModelAndView mav = new ModelAndView();
		mav.addObject("project", project);
		mav.setViewName("project");
		return mav;
	}
	
	@RequestMapping(value="{project}/execute",method = RequestMethod.POST)
	public String execute(@PathVariable("project") String projectName, @ModelAttribute Project projectModel) throws Exception {
		System.out.println(projectModel.getVersionsToExecute());
		Project project = projectRepo.getProject(projectName);
		List<Version> versionsToExecute = new ArrayList<Version>();
		for(Version version:project.getVersionsList()){
			if(projectModel.getVersionsToExecute().contains(version.getName())){
				versionsToExecute.add(version);
			}
		}
		Collections.reverse(versionsToExecute);
		DataAnalysis dataAnalysis = new DataAnalysis(null, project, versionsToExecute);
		dataAnalysis.start();
		
		return "redirect:/projects/"+projectName;
	}
	
	@RequestMapping(value="{project}/delete",method = RequestMethod.GET)
	public String deleteProject(@PathVariable("project") String projectName) throws Exception {
		projectRepo.deleteProject(projectName);
		return "redirect:/projects/list";
	}
	
	@RequestMapping(value="{project}/{version}/delete",method = RequestMethod.GET)
	public String deleteProjectVersion(@PathVariable("project") String projectName, @PathVariable("version") String version) throws Exception {
		projectRepo.deleteVersion(projectName,version);
		return "redirect:/projects/"+projectName;
	}

	@RequestMapping(value="{project}/{version}/updateRepo",method = RequestMethod.GET)
	public @ResponseBody UpdateResult updateRepo(@PathVariable("project") String projectName, @PathVariable("version") String version) throws Exception {
		try {
			UpdateResult result = projectRepo.updateRepo(projectName,version);
			return new UpdateResult(result.isSuccess(), replaceChars(result.getMessage()));
		} catch (Exception e) {
			return new UpdateResult(false, getStringFromException(e));
		}

	}

	@RequestMapping(method = RequestMethod.POST)
	public String save(@ModelAttribute Project project) throws Exception {
		projectRepo.save(project);
		return "redirect:/projects/"+project.getName();
	}
	
	@RequestMapping(value = "getBranches", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	Map getBranches(@RequestParam("vcs") String versionControlSystem,
			@RequestParam("url") String url) {

		VersionControlSystem versionControlSystemInstance = VersionControlSystem
				.getInstance(versionControlSystem, url);
		Map map = new HashMap<String, String>();
		try {
			map.put("branches", versionControlSystemInstance.getBranches());
			map.put("success", true);
		} catch (Exception e) {
			map.put("success", false);
			map.put("error",getStringFromException(e));
		}
		return map;
	}
	
	private String getStringFromException(Exception e){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String errorLog = sw.toString();
		errorLog = replaceChars(errorLog);
		return errorLog;
	}
	
	private String replaceChars(String msg){
		msg = msg.replaceAll("\n", "<br/>");
		msg = msg.replaceAll("\t", "&emsp;&emsp;");
		return msg;
	}
}