package com.glo.run

import grails.converters.JSON
import com.glo.ndo.*
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.web.json.JSONWriter



class MobileController {
	private static final logr = LogFactory.getLog(this)

	def index = {
		

	}

	def list = {
	}
	

	def int computeSumRecursive_impl2(Node root) {
		
		def result = root.value("TREE");
	
		for (Node n: root.children)
		  result += computeSumRecursive_impl2(n);
	
		return result;
	}
}
