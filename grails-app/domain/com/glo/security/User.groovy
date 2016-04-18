package com.glo.security

import com.glo.*
import com.glo.ndo.*


class User implements org.activiti.engine.identity.User {

	static auditable = true
	
	static searchable = [only: [
		'firstName',
		'lastName',
		'email',
		'username'
	]]

	String id
	String username
	String email
	String firstName
	String lastName
	String cellPhone
	String textTo
	String workPhone
	String password
	String tempPassword
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired

	static belongsTo = [
		company:Company
	]

	static constraints = {
		username blank: false, unique: true
		password blank: false
		email email: true, nullable:true, unique: true
		firstName nullable:true
		lastName nullable:true
		cellPhone nullable:true
		textTo nullable: true
		workPhone nullable:true
		company nullable: true
	}

	static mapping = {
		password column: '`password`'
		id generator: 'uuid', name:"id"
	}
	static transients = ["tempPassword"]

	Set<Role> getAuthorities() {
		UserRole.findAllByUser(this).collect { it.role } as Set
	}


	String toString() {
		 
		
		if (firstName || lastName)
			username + ' : ' + firstName + ' ' + lastName
		else
			username
	}
}
