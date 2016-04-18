package com.glo.security

import javax.servlet.ServletContext

class UserService {

	ServletContext servletContext
	
    static transactional = true

    def User get(def key, def sid) {
		
		User user
		if (key?.isLong())
			user = User.get(key.toLong())
		else
			user = User.findByUsername(key.toString())
			
		//TODO: avolos switch back security when ready	
//		if ( GrailsUtil.environment == "production" && 
//			(sid == null ||	servletContext.getAttribute("LoginUser_" + user.id) != sid))
//				return null
				
		return user
    }

	
    def validateUser (def username) {
		def user = get(username, "")
		if (!user || !user.enabled || user.accountExpired || user.accountLocked || user.passwordExpired) return (false)
		return (true)
	}

}