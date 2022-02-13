[![Build Status](https://travis-ci.com/anand1st/sshd-shell-spring-boot.svg?branch=master)](https://travis-ci.org/anand1st/sshd-shell-spring-boot)
[![Coverage Status](https://coveralls.io/repos/github/anand1st/sshd-shell-spring-boot/badge.svg?branch=master)](https://coveralls.io/github/anand1st/sshd-shell-spring-boot?branch=master)

# sshd-shell-spring-boot
This artifact is a spring boot starter that provides SSH access to spring boot applications. It is primarily designed
for  developers who want to roll out custom shell commands for various purposes.

The motivation for this starter was due to the fact that spring-boot had officially dropped support for
spring-boot-starter-remote-shell for the 2.x versions.

This starter has been tested with spring-boot 2.0.x to 2.6.x releases with support for its new endpoint infrastructure.

To import into Maven project, add the following dependency inside `pom.xml`:

    <dependency>
        <groupId>io.github.anand1st</groupId>
        <artifactId>sshd-shell-spring-boot-starter</artifactId>
        <version>4.2</version>
    </dependency>

For Gradle users, add these lines inside `build.gradle`:

    dependencies {
        compile group: 'io.github.anand1st', name: 'sshd-shell-spring-boot-starter', version: '4.2'
    }

### Note
Versions < 2.1 are deprecated and unsupported. The artifact above supports the following functionalities:

### Version 4.2
Supports Spring Boot 2.6.x versions.
Updated dependencies to jline 3.21.0 and, Apache SSHD 2.8.0.
Known issues: Circular dependency when used in Spring Boot 2.6 versions. Add the property `spring.mail.allow-circular-references=true`
to resolve this issue.

### Version 4.1
Supports Spring Boot 2.2.x versions.
Updated dependencies to jline 3.13.1, Apache SSHD 2.3.0.
Additional system commands for the following endpoints i.e. Flyway, IntegrationGraph, Liquibase, Sessions, Logfile and Prometheus:

    sshd.system.command.roles.flyway=${sshd.system.command.roles}
    sshd.system.command.roles.integrationGraph=${sshd.system.command.roles}
    sshd.system.command.roles.liquibase=${sshd.system.command.roles}
    sshd.system.command.roles.sessions=${sshd.system.command.roles}
    sshd.system.command.roles.logfile=${sshd.system.command.roles}
    sshd.system.command.roles.prometheus=${sshd.system.command.roles}

### Version 4.0
Updated dependencies to jline 3.11.0.
One of the biggest 'noise' of this library was the various management commands that were visible to all users.
Version 4.0 introduces some properties to help manage these commands (hereby called system commands).

    sshd.system.command.roles=ADMIN #Defaults to ADMIN, use comms-separated values for multiple admin roles
    sshd.system.command.roles.auditEvents=${sshd.system.command.roles}
    sshd.system.command.roles.beans=${sshd.system.command.roles}
    sshd.system.command.roles.caches=${sshd.system.command.roles}
    sshd.system.command.roles.conditionsReport=${sshd.system.command.roles}
    sshd.system.command.roles.configurationPropertiesReport=${sshd.system.command.roles}
    sshd.system.command.roles.environment=${sshd.system.command.roles}
    sshd.system.command.roles.health=${sshd.system.command.roles}
    sshd.system.command.roles.heapDump=${sshd.system.command.roles}
    sshd.system.command.roles.httpTrace=${sshd.system.command.roles}
    sshd.system.command.roles.info=${sshd.system.command.roles}
    sshd.system.command.roles.loggers=${sshd.system.command.roles}
    sshd.system.command.roles.mappings=${sshd.system.command.roles}
    sshd.system.command.roles.metrics=${sshd.system.command.roles}
    sshd.system.command.roles.scheduledTasks=${sshd.system.command.roles}
    sshd.system.command.roles.shutdown=${sshd.system.command.roles}
    sshd.system.command.roles.threadDump=${sshd.system.command.roles}

The user just need to set the `sshd.system.command.roles` to an admin role or to `*` if any authenticated user should
be able to have access to these commands. For individual configuration of management endpoints, setting the roles in
the above system command properties manually should suffice. Please note that these changes may affect the functionality
in previous versions of this library. Sufficient testing with this version is recommended.

### Version 3.7
Running with latest jline 3.10.0 and Apache SSHD version 2.2.0 and some code refactoring and cleanup. Fixed bug with
exception handling of ShellException and IllegalArgumentException in methods with SshdShellCommand annotation.
These exceptions can now be safely thrown and handled correctly by the CommandExecutor. Other exceptions are considered
unexpected and will result in a error log.

### Version 3.6
Getting a user's root directory for the purposes of SCP/SFTP is now made easier with the following approach:

    SshSessionContext.getUserDir()
Upgraded jline to 3.9.0 and Apache SSHD version to 2.0.0. `heapDump` command now zips the dump and places it into the
session user's directory if SCP/SFTP is enabled so that it can be downloaded.


### Version 3.5
Modified SshdShellProperties to add the properties

    sshd.filetransfer.enabled
    sshd.filesystem.base.dir
Added also username into SshSessionContext that can be accessed via

    SshSessionContext.get(Constants.USER)
With these changes, one can now figure out the rooted filesystem for a session user by autowiring SshdShellProperties
into the custom command classes to access the sshd.filesystem.base.dir property and by the session username.

### Version 3.4
Support for SFTP and SCP with configurable Root File System. Upgraded jline to 3.7.0. See Usage section below for
properties to be set. Tested with OSX only. Usage:

    sftp -P 8022 admin@localhost
    scp -P 8022 <localFile> admin@localhost:<filename>

### Version 3.3
Adheres to the management enabled endpoint for actuators. If the endpoint is enabled/disabled, the ssh command likewise
is enabled/disabled according to this setting.

### Version 3.2.1
Support for spring boot 2.0.0.RC1, 2.0.0.RC2 and 2.0.0.RELEASE.
Upgraded to jline 3.6.0 and sshd-core 1.7.0.
Minor code fixes and refactorings.

##### Please do not use version 3.2 as there seems to be some Jackson incompatibilities on Linux. Version 3.2.1 fixes
these issues.

### Version 3.1
Added support for SSH exec in addition to SSH shell.

### Version 3.0
Only for spring boot versions 2.0.0.M4 and (hopefully) above.
Added support for spring boot 2.0.0.M4 endpoint infrastructure which is very different from versions below this.

### Version 2.5
Supports spring boot versions from 1.5.x till 2.0.0.M3 (tested for these versions only).
Upgraded to jline-3.5.1.
Added support for highlighting of response output based on searched text using postprocessors (highlighting tested on
OSX). Added support for the emailing response output using postprocessors.
Examples of post processors can be seen below.

### Version 2.4
Upgraded to jline-3.4.0. Refactored I/O related activity from SshSessionContext to ConsoleIO. Separated console
processing into separate packages from SSH server packages.

### Version 2.3
Support for auto-completion of commands and subcommands using tab.

### Version 2.2
Fixed bug with prompt & text color.

### Version 2.1
Support for role-based access control using spring-security's `AuthenticationProvider` bean. `SshdShellCommand`
annotation includes a 'roles' parameter (defaults to * denoting permission to all commands) which should use
spring-security's role tag if `sshd.shell.authType=AUTH_PROVIDER` and spring-security is in the classpath. If a user's
role matches the roles in the command classes, he/she should be able to execute the command.

Every user session has a session context which a developer can use to manage state between command invocations

# Usage
All the developer needs to do it to create a class similar to below and make sure it's loaded by the Application
Context:

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
    sshd.filetransfer.enabled=true          # Defaults to false. Must be enabled for SCP and SFTP functionality
    sshd.filesystem.base.dir=/home/app      # Defaults to 'user.home' property

To connect to the application's SSH daemon (the port number can found from the logs when application starts up):

    ssh -p <port> <username>@<host>

If public key file is used for SSH daemon:

    ssh -p <port> -i <privateKeyFile> <username>@<host>

The following are sample inputs/outputs from the shell command if an admin user logs in:
    
    app> help
    Supported Commands
    admin                              Admin functionality. Type 'admin' for supported subcommands
    beans                              List beans
    caches                             Caches info
    conditionsReport                   Conditions report
    configurationPropertiesReport      Configuration properties report
    echo                               Echo by users. Type 'echo' for supported subcommands
    environment                        Environment details
    exit                               Exit shell
    flyway                             Flyway database migration details (if applicable)
    health                             System health info
    heapDump                           Heap dump command
    help                               Show list of help commands
    info                               System status
    integrationGraph                   Information about Spring Integration graph
    liquibase                          Liquibase database migration details (if applicable)
    logfile                            Application log file
    loggers                            Logging configuration
    mappings                           List http request mappings
    metrics                            Metrics operations
    prometheus                         Produces formatted metrics for scraping by Prometheus server
    scheduledTasks                     Scheduled tasks
    sessions                           Sessions management
    shutdown                           Shutdown application
    threadDump                         Print thread dump
    Supported post processors for output
    h <arg>                            Highlights <arg> in response output of command execution
                                       Example usage: help | h exit
    m <emailId>                        Send response output of command execution to <emailId>
                                       Example usage: help | m bob@hope.com
    app> echo
    Supported subcommand for echo
    admin                              Admin's echo. Usage: echo admin <arg>
    alice                              Alice's echo. Usage: echo alice <arg>
    bob                                Bob's echo. Usage: echo bob <arg>
    app> echo alice hi
    alice says hi
    app> echo alice hi | h alice
    alice says hi             ### alice is highlighted in yellow but colors can't be shown on markdown :-)
    app> echo alice hi | m bob@hope.com
    Output response sent to bob@hope.com
    app> echo bob hi
    What's your name? Jake
    bob echoes hi and your name is Jake, rooted filesystem path is /Users/user/admin
    app> echo alice hi
    alice says hi, Name Jake exists
    app> admin manage bob
    bob has been managed by admin
    

For a non-admin user, the following is seen in the `help` and `echo` subcommand as per settings of roles in the annotation:

    app> help
    Supported Commands
    echo                               Echo by users. Type 'echo' for supported subcommands
    exit                               Exit shell
    help                               Show list of help commands
    Supported post processors for output
    h <arg>                            Highlights <arg> in response output of command execution
                                       Example usage: help | h exit
    m <emailId>                        Send response output of command execution to <emailId>
                                       Example usage: help | m bob@hope.com
    server> echo
    Supported subcommand for echo
    alice                              Alice's echo. Usage: echo alice <arg>
    bob                                Bob's echo. Usage: echo bob <arg>
    app> admin manage Bob
    Permission denied
    app> exit
    Exiting shell

For more information, check out the sshd-shell-spring-boot-test-app project for a fully working example.

Limitations:
1) Currently, every method must return a java.lang.String (shell output) and take in exactly one java.lang.String
parameter. This parameter can be null at runtime.
2) Requires minimum JDK 8.
