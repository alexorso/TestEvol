package org.testevol.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.errors.TransportException;
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
import org.testevol.domain.RepositoryInfo;
import org.testevol.domain.Version;
import org.testevol.domain.VersionSettings;
import org.testevol.engine.DataAnalysis;
import org.testevol.engine.domain.Execution;
import org.testevol.engine.report.ExecutionStatus;
import org.testevol.versioncontrol.UpdateResult;
import org.testevol.versioncontrol.VersionControlSystem;

@Controller
@RequestMapping("/projects")
public class ProjectController {

	@Value("#{testEvolProperties.config_dir}")
	String configDir;

	@Autowired
	private ProjectRepository projectRepo;

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView init() {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("projects");
		return mav;
	}

	@RequestMapping(value = "list", method = RequestMethod.GET)
	public ModelAndView list(Principal principal) throws Exception {
		List<Project> projects = projectRepo.getProjects(principal.getName());

		ModelAndView mav = new ModelAndView();
		mav.addObject("projects", projects);
		mav.setViewName("projectList");
		return mav;
	}

	@RequestMapping(value = "names", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	List<String> getProjectNames(Principal principal) throws Exception {
		return projectRepo.getProjectsNames(principal.getName());
	}

	@RequestMapping(value = "{project}", method = RequestMethod.GET)
	public ModelAndView getProject(@PathVariable("project") String projectName,
			Principal principal) throws Exception {
		Project project = projectRepo.getProject(projectName, principal.getName());

		ModelAndView mav = new ModelAndView();
		mav.addObject("project", project);
		mav.setViewName("project");
		return mav;
	}

	@RequestMapping(value = "{project}/execute", method = RequestMethod.POST)
	public String execute(@PathVariable("project") final String projectName,
			final @ModelAttribute Project projectModel, final Principal principal)
			throws Exception {
		
		final String username = principal.getName();

		final Execution execution = projectRepo.createExecution(projectName,
				projectModel.getVersionsToExecute(), principal.getName());
		final Project project = execution.getProject();

		final List<Version> versionsToExecute = new ArrayList<Version>();
		for (Version version : project.getVersionsList()) {
			if (projectModel.getVersionsToExecute().contains(version.getName())) {
				version.setBaseBuildDir(execution.getExecutionDir());
				versionsToExecute.add(version);
			}
		}
		Collections.reverse(versionsToExecute);
		new Thread() {
			@Override
			public synchronized void run() {
				DataAnalysis dataAnalysis = new DataAnalysis(configDir,
						project, versionsToExecute,
						execution.getExecutionDir(),
						!projectModel.isIncludeCoverageAnalysis());
				try {
					projectRepo.saveExecution(projectName, execution.getId(),
							execution.getName(), ExecutionStatus.RUNNING, username);
					dataAnalysis.start();
					projectRepo.saveExecution(projectName, execution.getId(),
							execution.getName(), ExecutionStatus.SUCCESS, username);
				} catch (Exception e) {
					e.printStackTrace();
					try {
						projectRepo.saveExecution(projectName,
								execution.getId(), execution.getName(),
								ExecutionStatus.ERROR, username);
					} catch (Exception e1) {
					}
				}
			}
		}.start();

		return "redirect:/projects/" + projectName + "/execution/"
				+ execution.getId();
	}

	@RequestMapping(value = "{project}/delete", method = RequestMethod.GET)
	public String deleteProject(@PathVariable("project") String projectName,
			Principal principal) throws Exception {
		projectRepo.deleteProject(projectName, principal.getName());
		return "redirect:/projects/list";
	}

	@RequestMapping(value = "{project}/{version}/delete", method = RequestMethod.GET)
	public String deleteProjectVersion(
			@PathVariable("project") String projectName,
			@PathVariable("version") String version, Principal principal)
			throws Exception {
		projectRepo.deleteVersion(projectName, version, principal.getName());
		return "redirect:/projects/" + projectName;
	}

	@RequestMapping(value = "{project}/{version}/updateRepo", method = RequestMethod.GET)
	public @ResponseBody
	UpdateResult updateRepo(@PathVariable("project") String projectName,
			@PathVariable("version") String version, Principal principal)
			throws Exception {
		try {
			UpdateResult result = projectRepo.updateRepo(projectName, version, principal.getName());
			return new UpdateResult(result.isSuccess(),
					replaceChars(result.getMessage()));
		} catch (Exception e) {
			return new UpdateResult(false, getStringFromException(e));
		}
	}

	@RequestMapping(value = "{project}/executions", method = RequestMethod.GET)
	public ModelAndView getExecutions(
			@PathVariable("project") String projectName, Principal principal)
			throws Exception {
		Project project = projectRepo.getProject(projectName, principal.getName());
		List<Execution> executions = projectRepo.getExecutions(project, principal.getName());
		ModelAndView mav = new ModelAndView();
		mav.addObject("project", project);
		mav.addObject("executions", executions);
		mav.setViewName("executions");
		return mav;
	}

	@RequestMapping(value = "{project}/execution/{id}", method = RequestMethod.GET)
	public ModelAndView getExecution(
			@PathVariable("project") String projectName,
			@PathVariable("id") String id, Principal principal)
			throws Exception {
		Execution execution = projectRepo.getExecution(projectName, id, principal.getName());
		ModelAndView mav = new ModelAndView();
		mav.addObject("execution", execution);
		mav.setViewName("execution");
		return mav;
	}

	@RequestMapping(value = "{project}/execution/{id}/report", method = RequestMethod.GET)
	public ModelAndView getExecutionReport(
			@PathVariable("project") String projectName,
			@PathVariable("id") String id, Principal principal)
			throws Exception {
		Execution execution = projectRepo.getExecution(projectName, id, principal.getName());
		ModelAndView mav = new ModelAndView();
		mav.addObject("execution", execution);
		mav.setViewName("report");
		return mav;
	}

	@RequestMapping(value = "{project}/execution/{id}/report/csv", method = RequestMethod.GET)
	public void getFile(@PathVariable("project") String projectName,
			@PathVariable("id") String id, HttpServletResponse response,
			Principal principal) throws Exception {
		Execution execution = projectRepo.getExecution(projectName, id, principal.getName());
		try {
			// get your file as InputStream
			InputStream is = new FileInputStream(execution.getCSVReport());
			// copy it to response's OutputStream
			IOUtils.copy(is, response.getOutputStream());
			response.setContentType("text/plain");
			response.setHeader("Content-Disposition", "attachment; filename="
					+ execution.getName() + ".csv");
			response.flushBuffer();
		} catch (IOException ex) {
			throw new RuntimeException("Error while generating Report!");
		}

	}

	@RequestMapping(value = "{project}/execution/{id}/report/version", method = RequestMethod.GET)
	public ModelAndView getDetailedExecutionReport(
			@PathVariable("project") String projectName,
			@PathVariable("id") String id,
			@RequestParam("name") String version, Principal principal)
			throws Exception {
		Execution execution = projectRepo.getExecution(projectName, id, principal.getName());
		ModelAndView mav = new ModelAndView();
		mav.addObject("execution", execution);
		mav.addObject("version", version);
		mav.setViewName("detailed_report");
		return mav;
	}

	@RequestMapping(value = "{project}/execution/{id}/delete", method = RequestMethod.GET)
	public String deleteExecution(@PathVariable("project") String projectName,
			@PathVariable("id") String id, Principal principal) throws Exception {

		try {
			projectRepo.deleteExecution(projectName, id, principal.getName());
			return "redirect:/projects/" + projectName
					+ "/executions?success=true";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/projects/" + projectName
				+ "/executions?success=false";

	}

	@RequestMapping(value = "{project}/execution/{id}/save", method = RequestMethod.GET)
	public @ResponseBody
	Map saveExecution(@PathVariable("project") String projectName,
			@PathVariable("id") String id, @RequestParam("name") String name,
			Principal principal) {

		Map<String, Object> result = new HashMap<String, Object>();
		try {
			projectRepo.saveExecution(projectName, id, name, null, principal.getName());
			result.put("success", Boolean.TRUE);
		} catch (Exception e) {
			result.put("success", Boolean.FALSE);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(value = "{project}/execution/{id}/status", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	Map getExecutionStatus(@PathVariable("project") String projectName,
			@PathVariable("id") String id, Principal principal) {

		Execution execution;
		Map<String, Object> executionMap = new HashMap<String, Object>();
		try {
			execution = projectRepo.getExecution(projectName, id, principal.getName());
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

	@RequestMapping(value = "{project}/execution/{id}/report/script/{name}", method = RequestMethod.GET, produces = "application/javascript")
	public @ResponseBody
	String getReportScript(@PathVariable("project") String projectName,
			@PathVariable("id") String id,
			@PathVariable("name") String scriptName, Principal principal)
			throws Exception {

		Execution execution = projectRepo.getExecution(projectName, id, principal.getName());
		return FileUtils.readFileToString(new File(execution.getExecutionDir(),
				scriptName + ".js"));
	}

	@RequestMapping(method = RequestMethod.POST)
	public String save(@ModelAttribute Project project,
			@ModelAttribute RepositoryInfo repositoryInfo, Principal principal) throws Exception {
		project.setRepositoryInfo(repositoryInfo);
		projectRepo.save(project, principal.getName());
		return "redirect:/projects/" + project.getName();
	}

	@RequestMapping(value = "{project}/version/save_settings", method = RequestMethod.POST)
	public String saveSettings(@ModelAttribute VersionSettings versionSettings,
			Principal principal) throws Exception {
		projectRepo.updateVersionSettings(versionSettings, principal.getName());
		return "redirect:/projects/" + versionSettings.getProject();
	}

	@RequestMapping(value = "getBranches", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Map getBranches(@ModelAttribute RepositoryInfo repositoryInfo,
			Principal principal) {
		VersionControlSystem versionControlSystemInstance = VersionControlSystem
				.getInstance(repositoryInfo);
		Map map = new HashMap<String, String>();
		try {
			map.put("branches", versionControlSystemInstance.getBranches());
			map.put("success", true);
		} catch (TransportException e) {
			String message = e.getMessage();
			if (message.contains("Auth fail")
					|| message.contains("not authorized")) {
				map.put("auth_fail", true);
			}
			map.put("success", false);
			map.put("error", getStringFromException(e));
		} catch (Exception e) {
			map.put("success", false);
			map.put("error", getStringFromException(e));
		}

		return map;
	}

	private String getStringFromException(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String errorLog = sw.toString();
		errorLog = replaceChars(errorLog);
		return errorLog;
	}

	private String replaceChars(String msg) {
		msg = msg.replaceAll("\n", "<br/>");
		msg = msg.replaceAll("\t", "&emsp;&emsp;");
		return msg;
	}
}