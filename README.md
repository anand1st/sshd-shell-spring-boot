[![Build Status](https://travis-ci.org/anand1st/sshd-shell-spring-boot.svg?branch=master)](https://travis-ci.org/anand1st/sshd-shell-spring-boot)
[![Coverage Status](https://coveralls.io/repos/github/anand1st/sshd-shell-spring-boot/badge.svg?branch=master)](https://coveralls.io/github/anand1st/sshd-shell-spring-boot?branch=master)

# sshd-shell-spring-boot
This artifact is a spring boot starter that provides SSH access to spring boot applications. It is primarily designed for  developers who want to roll out custom shell commands for various purposes.

The motivation for this starter was due to the fact that spring-boot had officially dropped support for spring-boot-starter-remote-shell for the 2.x versions.

This starter has been tested with spring-boot 2.0.0.BUILD-SNAPSHOT, 2.0.0.M1 and 1.5.3.RELEASE. In theory however, it should work with any older releases.

# Usage
All the developer needs to do it to create a class similar to below and make sure it's loaded by the Application Context:

    @Component
    @SshdShellCommand(value = "echo", description = "Echo by users. Type 'echo help' for supported subcommands")
    public class EchoCommand {
    
        @SshdShellCommand(value = "bob", description = "Bob's echo. Usage: echo bob <arg>")
        public String bobSays(String arg) {
            return "bob says " + arg;  
        }
    
        @SshdShellCommand(value = "alice", description = "Alice's echo. Usage: echo alice <arg>")
        public String aliceSays(String arg) {
            return "alice says " + arg;
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
    sshd.shell.prompt.color=default		#Supports black,red,green,yellow,blue,purple,cyan,white
    sshd.shell.prompt.title=app
    sshd.shell.text.color=default 		#Supports black,red,green,yellow,blue,purple,cyan,white

For version 1.1, support for Spring Boot Banners has been added. Custom prompt and text color support has been removed and substituted with those from Spring Boot itself. The following properties need to be modified as the following:

    sshd.shell.prompt.color=DEFAULT		# See org.springframework.boot.ansi.AnsiColor for more options
    sshd.shell.text.color=DEFAULT

To connect to the application's SSH daemon (the port number can found from the logs when application starts up):

    ssh -p <port> <username>@<host>
or

    ssh -p <port> -i <privateKeyFile> <username>@<host>
    
if public key file is used for SSH daemon.

The following are sample inputs/outputs from the shell command:

    app> help
    Supported Commands
    echo		Echo by users. Type 'echo help' for supported subcommands
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

To import into Maven project, add the following dependency inside pom.xml:

    <dependency>
        <groupId>io.github.anand1st</groupId>
        <artifactId>sshd-shell-spring-boot-starter</artifactId>
        <version>1.1</version>
    </dependency>

Limitations:
1) Currently, every method must return a java.lang.String (shell output) and take in exactly one java.lang.String parameter (denoting arguments in shell command that are possibly null).
2) Requires minimum JDK 8.
