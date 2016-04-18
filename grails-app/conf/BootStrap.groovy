
import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.commons.cfg.*
import org.apache.log4j.LogManager
import org.apache.log4j.PropertyConfigurator
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import grails.converters.JSON
import com.mongodb.*

class BootStrap {

	def grailsApplication
	
    def init = { servletContext ->


		
		File configFile = new File("/usr/local/jd/conf/glo-config.properties");
		if (configFile.exists()) {
			
			// Reset log manager
			def props = new Properties()
			InputStream is = new BufferedInputStream(new FileInputStream("/usr/local/jd/conf/glo-config.properties"))
			props.load(is)
			is.close()
			
			LogManager.resetConfiguration()
			PropertyConfigurator.configure(configFile.toURI().toURL())			
			
			def config = new ConfigSlurper().parse(props)
			grailsApplication.config = grailsApplication.config.merge(config)
			ConfigurationHelper.initConfig(grailsApplication.config)
		}
		
    }
    def destroy = {
    }
}
