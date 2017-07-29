[![Build Status](https://travis-ci.org/anand1st/sshd-shell-spring-boot.svg?branch=master)](https://travis-ci.org/anand1st/sshd-shell-spring-boot)
[![Coverage Status](https://coveralls.io/repos/github/anand1st/sshd-shell-spring-boot/badge.svg?branch=master)](https://coveralls.io/github/anand1st/sshd-shell-spring-boot?branch=master)

# sshd-shell-spring-boot
This artifact is a spring boot starter that provides SSH access to spring boot applications. It is primarily designed for  developers who want to roll out custom shell commands for various purposes.

The motivation for this starter was due to the fact that spring-boot had officially dropped support for spring-boot-starter-remote-shell for the 2.x versions.

This starter has been tested with spring-boot 2.0.0.BUILD-SNAPSHOT, 2.0.0.M2 and 1.5.x.RELEASE. In theory however, it should work with any older releases.

To import into Maven project, add the following dependency inside pom.xml:

    <dependency>
        <groupId>io.github.anand1st</groupId>
        <artifactId>sshd-shell-spring-boot-starter</artifactId>
        <version>1.5</version>
    </dependency>

# Note
Versions < 1.3 are deprecated and unsupported. This document will only hold supporting information greater than version 1.3.

# Version 1.5
Minor refactorization. Exposed API for writing output i.e. SshSessionContext.writeOutput method.

# Version 1.4
In version 1.4, major changes have been made. They include 
1) Support for authentication using spring-security's AuthenticationProvider. To use this, set the following property in application.properties

       sshd.shell.auth.authType=DAO
2) Support for role-based access control. SshdShellCommand annotation now includes a 'roles' parameter (defaults to * denoting permission to all commands) which should use spring-security's role tag. If a user's role matches the roles in the command classes, he/she should be able to execute the command. See #Usage

# Version 1.3
In version 1.3, SshSessionContext has been introduced. This allows for states to be maintained in between commands. Support for interactive input/output for command has also been introduced via SshSessionContext.readInput method. Masking of input characters supported. Examples can be seen in sample test application and below.

# Usage
All the developer needs to do it to create a class similar to below and make sure it's loaded by the Application Context:

    @Component
    @SshdShellCommand(value = "echo", description = "Echo by users. Type 'echo help' for supported subcommands")
    public class EchoCommand {
    
        @SshdShellCommand(value = "bob", description = "Bob's echo. Usage: echo bob <arg>")
        public String bobSays(String arg) throws IOException {
	        SshSessionContext.writeOutput("Need user info");
            String name = SshSessionContext.readInput("What's your name?");
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
    sshd.shell.prompt.color=DEFAULT		# See org.springframework.boot.ansi.AnsiColor for more options
    sshd.shell.text.color=DEFAULT
    sshd.shell.auth.authType=SIMPLE		# Since v1.4. Possible values: SIMPLE, DAO
    sshd.shell.auth.authProviderBeanName=	# Since v1.4. Bean name of authentication provider if authType is DAO (optional)
    
When spring-boot-actuator is included, HealthIndicator classes in classpath will be loaded. The 'health' command will show all HealthIndicator components.

To connect to the application's SSH daemon (the port number can found from the logs when application starts up):

    ssh -p <port> <username>@<host>

If public key file is used for SSH daemon:

    ssh -p <port> -i <privateKeyFile> <username>@<host>

The following are sample inputs/outputs from the shell command if a non-admin user logs in:

    app> help
    Supported Commands
    echo		Echo by users. Type 'echo help' for supported subcommands
    health		Display health services
    exit		Exit shell
    app> echo
    Supported subcommand for echo
            alice    Alice's echo. Usage: echo alice <arg>
            bob      Bob's echo. Usage: echo bob <arg>
    app> echo alice hi
    alice says hi
    app> echo bob hi
    What's your name? Jake
    bob echoes hi and your name is Jake
    app> echo alice hi
    alice says hi, Name Jake exists
    app> admin manage
    Permission denied

For an admin user, the following extras can be seen in the help and echo subcommand as per settings of roles in the annotation:

    app> help
    Supported Commands
    admin		Admin functionality. Type 'admin' for supported subcommands
    echo		Echo by users. Type 'echo' for supported subcommands
    exit		Exit shell
    health		Health of services
    help		Show list of help commands
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
