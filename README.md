[![Build Status](https://travis-ci.org/anand1st/sshd-shell-spring-boot.svg?branch=master)](https://travis-ci.org/anand1st/sshd-shell-spring-boot)
[![Coverage Status](https://coveralls.io/repos/github/anand1st/sshd-shell-spring-boot/badge.svg?branch=master)](https://coveralls.io/github/anand1st/sshd-shell-spring-boot?branch=master)

# sshd-shell-spring-boot
This artifact is a spring boot starter that provides SSH access to spring boot applications. It is primarily designed for  developers who want to roll out custom shell commands for various purposes.

The motivation for this starter was due to the fact that spring-boot had officially dropped support for spring-boot-starter-remote-shell for the 2.x versions.

This starter has been tested with spring-boot 2.0.0.M4 till 2.0.0.RC1 with support its new endpoint infrastructure. For supporting the older spring boot versions, please see the git branch 1.5.x which supports 1.5.x right up to 2.0.0.M3 for more information.

To import into Maven project, add the following dependency inside pom.xml:

    <dependency>
        <groupId>io.github.anand1st</groupId>
        <artifactId>sshd-shell-spring-boot-starter</artifactId>
        <version>3.2.1</version>
    </dependency>

### Note
Versions < 2.1 are deprecated and unsupported. The artifact above supports the following functionalities:

### Version 3.2.1
Support for spring boot 2.0.0.RC1
Upgraded to jline 3.6.0, sshd-core 1.7.0
Minor code fixes and refactorings.

### Please do not use version 3.2 as there seems to be some Jackson incompatibilities on Linux. Version 3.2.1 fixes these issues.

### Version 3.1
Added support for SSH exec in addition to SSH shell.

### Version 3.0
Only for spring boot versions 2.0.0.M4 and (hopefully) above.
Added support for spring boot 2.0.0.M4 endpoint infrastructure which is very different from versions below this.

### Version 2.5
Supports spring boot versions from 1.5.x till 2.0.0.M3 (tested for these versions only).
Upgraded to jline-3.5.1.
Added support for highlighting of response output based on searched text using postprocessors (highlighting tested on OSX).
Added support for the emailing response output using postprocessors.
Examples of post processors can be seen below.

### Version 2.4
Upgraded to jline-3.4.0. Refactored I/O related activity from SshSessionContext to ConsoleIO. Separated console processing into separate packages from SSH server packages.

### Version 2.3
Support for auto-completion of commands and subcommands using tab.

### Version 2.2
Fixed bug with prompt & text color.

### Version 2.1
Support for role-based access control using spring-security's `AuthenticationProvider` bean. `SshdShellCommand` annotation includes a 'roles' parameter (defaults to * denoting permission to all commands) which should use spring-security's role tag if `sshd.shell.authType=AUTH_PROVIDER` and spring-security is in the classpath. If a user's role matches the roles in the command classes, he/she should be able to execute the command. 

Every user session has a session context which a developer can use to manage state between command invocations

# Usage
All the developer needs to do it to create a class similar to below and make sure it's loaded by the Application Context:

    @Component
    @SshdShellCommand(value = "echo", description = "Echo by users. Type 'echo help' for supported subcommands")
    public class EchoCommand {
    
        @SshdShellCommand(value = "bob", description = "Bob's echo. Usage: echo bob <arg>")
        public String bobSays(String arg) throws IOException {
	        ConsoleIO.writeOutput("Need user info");
            String name = ConsoleIO.readInput("What's your name?");
            SshSessionContext.put("name", name);
            return "bob echoes " + arg + " and your name is " + name;
        }
    
        @SshdShellCommand(value = "alice", description = "Alice's echo. Usage: echo alice <arg>")
        public String aliceSays(String arg) {
            String str = "";
            if (SshSessionContext.containsKey("name")) {
                str = ", Name " + SshSessionContext.get("name") + " exists";
            }
            return "alice says " + arg + str;
        }
	
	    @SshdShellCommand(value = "admin", description = "Admin's echo. Usage: echo admin <arg>", roles = "ADMIN")
	    public String adminSays(String arg) {
	        return "admin says " + arg;
	    }
    }

Supported properties in application.properties (defaults are as below):

    sshd.shell.port=8022			#Set to 0 for random port
    sshd.shell.enabled=false		#Set this to true to startup SSH daemon
    sshd.shell.username=admin
    sshd.shell.password= 			#A random string is generated if password is not set and shown in logs
    sshd.shell.publicKeyFile=
    sshd.shell.host=127.0.0.1		#Allowed IP addresses
    sshd.shell.hostKeyFile=hostKey.ser
    sshd.shell.prompt.title=app
    
    # Supported values for colors: BLACK,RED,GREEN,YELLOW,BLUE,MAGENTA,CYAN,WHITE (leave blank for default)
    sshd.shell.prompt.color=           # Defaults to BLACK
    sshd.shell.text.color=             # Defaults to BLACK
    sshd.shell.text.highlightColor=    # Defaults to YELLOW
    
    sshd.shell.auth.authType=SIMPLE		# Possible values: SIMPLE, AUTH_PROVIDER
    sshd.shell.auth.authProviderBeanName=	# Bean name of authentication provider if authType is AUTH_PROVIDER (optional)
    
When spring-boot-actuator is included, `HealthIndicator` classes in classpath will be loaded. The 'health' command will show all `HealthIndicator` components. Developers can also write their own custom `HealthIndicator` classes for loading. It's important that the names of these custom classes end with the suffix `HealthIndicator` to be loaded by the application.

To connect to the application's SSH daemon (the port number can found from the logs when application starts up):

    ssh -p <port> <username>@<host>

If public key file is used for SSH daemon:

    ssh -p <port> -i <privateKeyFile> <username>@<host>

The following are sample inputs/outputs from the shell command if a non-admin user logs in:

    app> help
    admin                              Admin functionality. Type 'admin' for supported subcommands
    auditEvents                        Event auditing
    autoConfigurationReport            Autoconfiguration report
    beans                              List beans
    configurationPropertiesReport      Configuration properties report
    echo                               Echo by users. Type 'echo' for supported subcommands
    environment                        Environment details
    exit                               Exit shell
    health                             System health info
    help                               Show list of help commands
    info                               System status
    loggers                            Logging configuration
    metrics                            Metrics operations
    requestMapping                     Request mapping information
    shutdown                           Shutdown application
    status                             System status
    threadDump                         Print thread dump
    traces                             Trace information
    Supported post processors for output
    h <arg>                            Highlights <arg> in response output of command execution
                                       Example usage: help | h exit
    m <emailId>                        Send response output of command execution to <emailId>
                                       Example usage: help | m bob@hope.com
    app> echo
    Supported subcommand for echo
            alice    Alice's echo. Usage: echo alice <arg>
            bob      Bob's echo. Usage: echo bob <arg>
    app> echo alice hi
    alice says hi
    app> echo alice hi | h alice
    alice says hi             ### alice is highlighted in yellow but colors can't be shown on markdown :-)
    app> echo alice hi | m bob@hope.com
    Output response sent to bob@hope.com
    app> echo bob hi
    What's your name? Jake
    bob echoes hi and your name is Jake
    app> echo alice hi
    alice says hi, Name Jake exists
    app> admin manage
    Permission denied

For an admin user, the following extras can be seen in the help and echo subcommand as per settings of roles in the annotation:

    app> help
    ...
    admin		Admin functionality. Type 'admin' for supported subcommands
    ...
    app> echo
    Supported subcommand for echo
    admin		Admin's echo. Usage: echo admin <arg>
    alice		Alice's echo. Usage: echo alice <arg>
    bob		Bob's echo. Usage: echo bob <arg>
    app> admin
    Supported subcommand for admin
    manage		Manage task. Usage: admin manage <arg>
    app>

For more information, check out the sshd-shell-spring-boot-test-app project for a fully working example.

Limitations:
1) Currently, every method must return a java.lang.String (shell output) and take in exactly one java.lang.String parameter (denoting nullable arguments in shell command).
2) Requires minimum JDK 8.
