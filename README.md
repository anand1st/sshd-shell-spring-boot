[![Build Status](https://travis-ci.org/anand1st/sshd-shell-spring-boot.svg?branch=master)](https://travis-ci.org/anand1st/sshd-shell-spring-boot)
[![Coverage Status](https://coveralls.io/repos/github/anand1st/sshd-shell-spring-boot/badge.svg?branch=master)](https://coveralls.io/github/anand1st/sshd-shell-spring-boot?branch=master)

# sshd-shell-spring-boot
This artifact is a spring boot starter that provides SSH access to spring boot applications. It is primarily designed for  developers who want to roll out custom shell commands for various purposes.

The motivation for this starter was due to the fact that spring-boot had officially dropped support for spring-boot-starter-remote-shell for the 2.x versions.

This starter has been tested with spring-boot 2.0.0.BUILD-SNAPSHOT, 2.0.0.M1 and 1.5.x.RELEASE. In theory however, it should work with any older releases.

To import into Maven project, add the following dependency inside pom.xml:

    <dependency>
        <groupId>io.github.anand1st</groupId>
        <artifactId>sshd-shell-spring-boot-starter</artifactId>
        <version>1.2</version>
    </dependency>

# Note
Versions < 1.2 are deprecated and unsupported. Please upgrade to the latest. This document will only hold supporting information for latest release and snapshot versions.

SshSessionContext has been introduced. This allows us for states to be maintained in between commands. Support for interactive input output for command has also been introduced via SshSessionContext.readInput method. Examples can be seen in sample test application and below.

# Usage
All the developer needs to do it to create a class similar to below and make sure it's loaded by the Application Context:

    @Component
    @SshdShellCommand(value = "echo", description = "Echo by users. Type 'echo help' for supported subcommands")
    public class EchoCommand {
    
        @SshdShellCommand(value = "bob", description = "Bob's echo. Usage: echo bob <arg>")
        public String bobSays(String arg) throws IOException {
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
    }

Supported properties in application.properties (defaults are as below) for version 1.0:

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
    
When spring-boot-actuator is included, HealthIndicator classes in classpath will be loaded. The 'health' command will show all HealthIndicator components.

To connect to the application's SSH daemon (the port number can found from the logs when application starts up):

    ssh -p <port> <username>@<host>

If public key file is used for SSH daemon:

    ssh -p <port> -i <privateKeyFile> <username>@<host>

The following are sample inputs/outputs from the shell command:

    app> help
    Supported Commands
    echo		Echo by users. Type 'echo help' for supported subcommands
    health		Display health services
    exit		Exit shell
    app> echo help
    Echo by users. Type 'echo help' for supported subcommands
            bob      Bob's echo. Usage: echo bob <arg>
	        alice    Alice's echo. Usage: echo alice <arg>
    app> echo alice hi
    alice says hi
    app> echo bob hi
    bob says hi
    app>

Limitations:
1) Currently, every method must return a java.lang.String (shell output) and take in exactly one java.lang.String parameter (denoting nullable arguments in shell command).
2) Requires minimum JDK 8.
