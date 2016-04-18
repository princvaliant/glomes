package com.glo.run

import grails.converters.*
import org.apache.commons.logging.LogFactory

public class Rest {
	
	private static final logr = LogFactory.getLog(this)


	public def success(String data) {
		
		!logr.isDebugEnabled() ?: logr.debug(data)
		
		response.status = 200
		render (text:formatJson(data, "true", "\"\"", "200"), contentType:"text/json") 

	}
	
	public def success(JSON data) {
		
		response.status = 200
		render (text:formatJson(data.toString(), "true", "\"\"", "200"), contentType:"text/json") 
		
		data.setPrettyPrint(true)
		!logr.isDebugEnabled() ?: logr.debug(data.toString(true))
	}
	
	public def created(String data) {
		
		!logr.isDebugEnabled() ?: logr.debug(data)
		
		response.status = 201
		render (text:formatJson(data, "true", "\"\"", "201"), contentType:"text/json")
	}
	
	public def created(JSON data) {
		
		response.status = 201
		render (text:formatJson(data.toString(), "true", "\"\"", "201"), contentType:"text/json")
		
		data.setPrettyPrint(true)
		!logr.isDebugEnabled() ?: logr.debug(data.toString(true))
	}
	
	public def notFound(String message)
	{
		!logr.isWarnEnabled() ?: logr.warn(message)
		
		response.status = 404
		render (text:formatJson("{}", "false", "\"${message}\"", "404"), contentType:"text/json")
	}
	
	public def forbidden(String message) {
		
		!logr.isWarnEnabled() ?: logr.warn(message)
		
		response.status = 403
		render (text:formatJson("{}", "false", "\"${message}\"", "403"), contentType:"text/json")
	}
	
	public def error(String message)
	{
		logr.error(message)
		
		return [success:false, msg: message] as JSON
	}
	
	public Boolean validation( domain) {	
			
		if (!domain.validate())
		{
			response.status = 403
			String str = domain.errors.allErrors.collect {
				message(error:it,encodeAs:'HTML')
			} as JSON
		
			!logr.isWarnEnabled() ?: logr.warn(str.toString())
			
			render (text:formatJson("{}", "failed", str, "200"), contentType:"text/json")
			return false
		}
		return true
	}
	
	private formatJson(data, status, message, code)
	{
		if (status == "false") {
			return "[\"success\":${status},\"msg\":${message}]"
		} else {
			return "{\"success\":${status},\"data\":${data},\"msg\":${message}}"
		}
	}
}
