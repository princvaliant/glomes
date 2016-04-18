package com.glo.security

import java.util.Set;

class Role implements org.activiti.engine.identity.Group {

	static auditable = true
	
	String id
	String authority
	String name
	String type
	

	static constraints = {
		authority blank: false, unique: true
		name blank: false
		type nullable: true
	}
	

	static mapping = {
		cache true
		id generator: 'assigned', name:"id"
	}

	String toString() {
		name +  ", " + authority
	}
	
	Set<User> getUsers() {
		UserRole.findAllByRole(this).collect { it.user } as Set
	}


}
