package org.testevol.controller;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.testevol.engine.domain.Execution;
import org.testevol.engine.report.ExecutionStatus;
import org.testevol.versioncontrol.UpdateResult;
import org.testevol.versioncontrol.VersionControlSystem;

@Controller
@RequestMapping("/projects")
public class ProjectController {
	
	@Value("#{testEvolProperties.config_dir}") String configDir;
	
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

	@RequestMapping(value="names",method = RequestMethod.GET, produces="application/json")
	public @ResponseBody List<String> getProjectNames() throws Exception {
		return projectRepo.getProjectsNames();
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
	public String execute(@PathVariable("project") final String projectName, final @ModelAttribute Project projectModel) throws Exception {

		final Execution execution = projectRepo.createExecution(projectName, projectModel.getVersionsToExecute());
		final Project project = execution.getProject();
		
		final List<Version> versionsToExecute = new ArrayList<Version>();
		for(Version version:project.getVersionsList()){
			if(projectModel.getVersionsToExecute().contains(version.getName())){
				version.setBaseBuildDir(execution.getExecutionDir());
				versionsToExecute.add(version);
			}
		}
		Collections.reverse(versionsToExecute);
		new Thread(){
			@Override
			public synchronized void run() {
				DataAnalysis dataAnalysis = new DataAnalysis(configDir, project, versionsToExecute, execution.getExecutionDir(), !projectModel.isIncludeCoverageAnalysis());
				try {
					projectRepo.saveExecution(projectName, execution.getId(), execution.getName(), ExecutionStatus.RUNNING);
					dataAnalysis.start();
					projectRepo.saveExecution(projectName, execution.getId(), execution.getName(), ExecutionStatus.SUCCESS);
				} catch (Exception e) {
					e.printStackTrace();
					try {
						projectRepo.saveExecution(projectName, execution.getId(), execution.getName(), ExecutionStatus.ERROR);
					} catch (Exception e1) {}
				}
			}
		}.start();

		
		return "redirect:/projects/"+projectName+"/execution/"+execution.getId();
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

	@RequestMapping(value="{project}/executions",method = RequestMethod.GET)
	public ModelAndView getExecutions(@PathVariable("project") String projectName) throws Exception {
		Project project = projectRepo.getProject(projectName);
		List<Execution> executions = projectRepo.getExecutions(project);
		ModelAndView mav = new ModelAndView();
		mav.addObject("project", project);
		mav.addObject("executions", executions);
		mav.setViewName("executions");
		return mav;
	}
	
	@RequestMapping(value="{project}/execution/{id}",method = RequestMethod.GET)
	public ModelAndView getExecution(@PathVariable("project") String projectName, @PathVariable("id") String id) throws Exception {
		Execution execution = projectRepo.getExecution(projectName, id);
		ModelAndView mav = new ModelAndView();
		mav.addObject("execution", execution);
		mav.setViewName("execution");
		return mav;
	}
	
	@RequestMapping(value="{project}/execution/{id}/report",method = RequestMethod.GET)
	public ModelAndView getExecutionReport(@PathVariable("project") String projectName, @PathVariable("id") String id) throws Exception {
		Execution execution = projectRepo.getExecution(projectName, id);
		ModelAndView mav = new ModelAndView();
		mav.addObject("execution", execution);
		mav.setViewName("report");
		return mav;
	}
	
	@RequestMapping(value="{project}/execution/{id}/report/version",method = RequestMethod.GET)
	public ModelAndView getDetailedExecutionReport(@PathVariable("project") String projectName, 
													@PathVariable("id") String id,
													@RequestParam("name") String version) throws Exception {
		Execution execution = projectRepo.getExecution(projectName, id);
		ModelAndView mav = new ModelAndView();
		mav.addObject("execution", execution);
		mav.addObject("version", version);
		mav.setViewName("detailed_report");
		return mav;
	}	
	
	@RequestMapping(value="{project}/execution/{id}/delete",method = RequestMethod.GET)
	public String deleteExecution(@PathVariable("project") String projectName, @PathVariable("id") String id) throws Exception {
		
		try {
			projectRepo.deleteExecution(projectName, id);
			return "redirect:/projects/"+projectName+"/executions?success=true";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/projects/"+projectName+"/executions?success=false";

	}
	
	@RequestMapping(value="{project}/execution/{id}/save",method = RequestMethod.GET)
	public @ResponseBody Map saveExecution(	@PathVariable("project") String projectName, 
										@PathVariable("id") String id, 
										@RequestParam("name") String name){
		
		Map<String, Object> result = new HashMap<String, Object>(); 
		try {
			projectRepo.saveExecution(projectName, id, name, null);
			result.put("success", Boolean.TRUE);
		} catch (Exception e) {
			result.put("success", Boolean.FALSE);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return result;
	}
	
	@RequestMapping(value="{project}/execution/{id}/status",method = RequestMethod.GET, produces="application/json")
	public @ResponseBody Map getExecutionStatus(@PathVariable("project") String projectName, @PathVariable("id") String id){
		
		Execution execution;
		Map<String, Object> executionMap = new HashMap<String, Object>();
		try {
			execution = projectRepo.getExecution(projectName, id);
			ExecutionStatus status = execution.getStatus();			
			executionMap.put("code", status.getCode());
			executionMap.put("label", status.getLabel());
			executionMap.put("style", status.getStyle());
			executionMap.put("req_success", Boolean.TRUE);
			executionMap.put("log", execution.getExecutionLog());
			
		} catch (Exception e) {
			executionMap.put("req_success", Boolean.FALSE);
		}
		return executionMap;
	}
	
	@RequestMapping(value = "{project}/execution/{id}/report/script/{name}", method = RequestMethod.GET, produces="application/javascript")
	public @ResponseBody
	String getReportScript(@PathVariable("project") String projectName,
			@PathVariable("id") String id,
			@PathVariable("name") String scriptName) throws Exception {
		
		Execution execution = projectRepo.getExecution(projectName, id);
		return FileUtils.readFileToString(new File(execution.getExecutionDir(),scriptName+".js"));
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