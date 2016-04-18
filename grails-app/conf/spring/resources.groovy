import org.apache.commons.dbcp.BasicDataSource
import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.jms.connection.CachingConnectionFactory

beans = {

	def props = new Properties()
	InputStream is = new BufferedInputStream(new FileInputStream("/usr/local/jd/conf/glo-config.properties"))
	props.load(is)
	is.close()
	

	dataSourceActiviti(BasicDataSource) {
		driverClassName = "com.mysql.jdbc.Driver"
		url = props.get("dataSourceUrl")
		username =  props.get("dataSourceUsername")
		password = props.get("dataSourcePassword")
	}
	
	
	jmsConnectionFactory(CachingConnectionFactory) {
        targetConnectionFactory = { ActiveMQConnectionFactory cf ->
            brokerURL = 'vm://localhost'
        }
    }
}
