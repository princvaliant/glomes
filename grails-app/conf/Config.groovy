grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
        all: '*/*',
        atom: 'application/atom+xml',
        css: 'text/css',
        csv: 'text/csv',
        form: 'application/x-www-form-urlencoded',
        html: ['text/html', 'application/xhtml+xml'],
        js: 'text/javascript',
        json: ['application/json', 'text/json'],
        multipartForm: 'multipart/form-data',
        rss: 'application/rss+xml',
        text: 'text/plain',
        xml: ['text/xml', 'application/xml']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000
grails.project.war.file = "target/glo.war"

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'
grails.converters.default.pretty.print = true

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart = false
// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true

grails {
    mail {



        host = 'mail.valleytechllc.com'
        port = 25
        username = 'alerts_gl@valleytechllc.com'
        password = 'nE2uhec3umux'


//        host = "192.168.20.21"
//        port = 25


//        host = "192.168.0.125"
//        port = 25
      //  username = "qunano\\alerts"
      //  password = "4U12124up!"
        props = [
                "mail.smtp.auth":"false",
                "mail.smtp.starttls.enable":"false",
                "mail.smtp.ssl.enable":"false"
        ]
    }
}

grails.mail.default.from = "alerts@glo.se"
grails.mail.default.to = "alerts@glo.se"

//grails.mail.default.from = "gl_alerts@valleytechllc.com"
//grails.mail.default.to = "gl_alerts@valleytechllc.com"

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

tomcat.deploy.username = "admin"
tomcat.deploy.password = "1"
tomcat.deploy.url = "http://calserver03:8081/manager/text"

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //

    appenders {
        console name: 'S', layout: pattern(conversionPattern: '%d %-5p %c - %m%n')
        rollingFile name: "R", maxFileSize: 500000, file: '/var/logs/glo.log'
    }

    environments {
        production { debug R: ['com.glo'] }
        development { debug S: ['com.glo'] }
    }

    error 'org.codehaus.groovy.grails.web.servlet',  //  controllers
            'org.codehaus.groovy.grails.web.pages', //  GSP
            'org.codehaus.groovy.grails.web.sitemesh', //  layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping', // URL mapping
            'org.codehaus.groovy.grails.commons', // core / classloading
            'org.codehaus.groovy.grails.plugins', // plugins
            'org.springframework',
            'net.sf.ehcache.hibernate',
            'org.grails.datastore.gorm.support.DatastorePersistenceContextInterceptor',
            'grails.app',
            'org.codehaus.groovy.grails.orm.hibernate',
            'org.hibernate'

    warn 'org.mortbay.log'


}

environments {
    production {
        grails.serverURL = "http://mes/${appName}"
    }
    development {
        grails.serverURL = "http://localhost:8080/${appName}"
    }
    test {
        grails.serverURL = "http://localhost:8080/${appName}"
    }
}

// Added by the Spring Security Core plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = 'com.glo.security.User'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'com.glo.security.UserRole'
grails.plugins.springsecurity.authority.className = 'com.glo.security.Role'
grails.plugins.springsecurity.securityConfigType = 'InterceptUrlMap'
grails.plugins.springsecurity.rejectIfNoRule = true

// Remember me configuration
grails.plugins.springsecurity.rememberMe.cookieName = 'glo_remember_me'
grails.plugins.springsecurity.rememberMe.alwaysRemember = true
grails.plugins.springsecurity.rememberMe.tokenValiditySeconds = 31 * 24 * 60 * 60
grails.plugins.springsecurity.rememberMe.parameter = '_spring_security_remember_me'
grails.plugins.springsecurity.rememberMe.key = 'gloApp'
grails.plugins.springsecurity.rememberMe.useSecureCookie = true

grails.plugins.springsecurity.interceptUrlMap = [
        '/*': [
                'IS_AUTHENTICATED_FULLY'
        ],
        '/productFamily/**': [
                'ROLE_ADMIN',
                'ROLE_FAB_ADMIN',
                'ROLE_EQUIPMENT_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/product/**': [
                'ROLE_ADMIN',
                'ROLE_FAB_ADMIN',
                'ROLE_EQUIPMENT_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/bom/**': [
                'ROLE_ADMIN',
                'ROLE_FAB_ADMIN',
                'ROLE_EQUIPMENT_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/bomPart/**': [
                'ROLE_ADMIN',
                'ROLE_FAB_ADMIN',
                'ROLE_EQUIPMENT_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/productCompany/**': [
                'ROLE_ADMIN',
                'ROLE_FAB_ADMIN',
                'ROLE_EQUIPMENT_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/user/**': [
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/role/**': [
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/process/**': [
                'ROLE_ADMIN',
                'ROLE_PROCESS_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/processStep/**': [
                'ROLE_ADMIN',
                'ROLE_PROCESS_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/variable/**': [
                'ROLE_ADMIN',
                'ROLE_PROCESS_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/yieldLossReason/**': [
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/bonusReason/**': [
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/reworkReason/**': [
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/equipment/**': [
                'ROLE_ADMIN',
                'ROLE_EQUIPMENT_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/company/**': [
                'ROLE_ADMIN',
                'ROLE_FAB_ADMIN',
                'ROLE_EQUIPMENT_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/location/**': [
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/deploy/**': [
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/operation/**': [
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/workCenter/**': [
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/equipmentPart/**': [
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/equipmentActivePart/**': [
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/equipmentFailure/**': [
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/equipmentUnscheduled/**': [
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/productMask/**': [
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/productMaskItem/**': [
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/report/**': [
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/mobile/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/mobile/js/extm/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/mobile/js/app/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/userConsole/**': [
                'ROLE_USER',
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/userConsole/js/app/**': [
                'ROLE_USER',
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/userConsole/js/ext/**': [
                'ROLE_USER',
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/admin/**': [
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/uploadWaferStartInventory/**': [
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/dataCorrection/**': [
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/renameProduct/**': [
                'ROLE_ADMIN',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/uploadQev/**': [
                'ROLE_USER',
                'QEV_OPERATOR',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/reportPdfProcessor/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/dataFixer/**': [
                'ROLE_USER',
                'QEV_OPERATOR',
                'IS_AUTHENTICATED_FULLY'
        ],
        '/js/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/css/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/img/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/login/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/logout/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/plugins/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/activiti/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/taskbooks/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/variables/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/transitions/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/units/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/productCompanies/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/softwareRequest/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/productFamilies/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/products/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/locations/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/equipments/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/metaItems/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/epiRuns/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/epiParser/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/dataViews/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/dataEntry/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/operations/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/companies/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/users/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/contents/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/dataViewCharts/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/charts/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/notes/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/files/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/yieldLosses/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/bonuses/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/reworks/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/synchronize/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/inv/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/wafer/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/spc/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/probeTest/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/relTest/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/lampTest/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/analyser/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/import/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/dataImport/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/photo/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/topImages/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/probeTestImages/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/uniformityImages/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/relImages/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/epidash/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/measure/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/dataFixer/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/releaseNotes.html': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ],
        '/testDataImages/**': [
                'IS_AUTHENTICATED_ANONYMOUSLY'
        ]
]

// Added by the Grails Activiti plugin:
activiti {
    processEngineName = "activiti-engine-default"
    databaseType = "mysql"
    deploymentName = appName
    deploymentResources = [
            "file:./grails-app/conf/**/*.bpmn*.xml",
            "file:./grails-app/conf/**/*.png",
            "file:./src/taskforms/**/*.form"
    ]
    jobExecutorActivate = false
    mailServerHost = "smtp.yourserver.com"
    mailServerPort = "25"
    mailServerUsername = ""
    mailServerPassword = ""
    mailServerDefaultFrom = "username@yourserver.com"
    history = "full" // "none", "activity", "audit" or "full"
    sessionUsernameKey = "username"
    useFormKey = true
}

environments {
    development {
        activiti {
            processEngineName = "activiti-engine-dev"
            databaseSchemaUpdate = false // true, false or "create-drop"
        }
    }
    test {
        activiti {
            processEngineName = "activiti-engine-test"
            databaseSchemaUpdate = false
            mailServerPort = "5025"
        }
    }
    production {
        activiti {
            processEngineName = "activiti-engine-prod"
            databaseSchemaUpdate = false
            //		  jobExecutorActivate = true
        }
    }
}

// Uncomment and edit the following lines to start using Grails encoding & escaping improvements

/* remove this line 
// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside null
                scriptlet = 'none' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        filteringCodecForContentType {
            //'text/html' = 'html'
        }
    }
}
remove this line */
