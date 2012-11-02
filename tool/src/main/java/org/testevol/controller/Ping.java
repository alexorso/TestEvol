package org.testevol.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/ping")
public class Ping {

	@RequestMapping(method = RequestMethod.GET, produces = "text/xml")
	public @ResponseBody
	String ping() {
		return "<pingdom_http_custom_check><status>OK</status><response_time>1</response_time></pingdom_http_custom_check>";
	}

}