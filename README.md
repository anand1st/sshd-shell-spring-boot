# sshd-shell-spring-boot
This artifact is a spring boot starter that provides SSH access to spring boot applications. It is primarily designed for  developers who want to roll out custom shell commands for various purposes.

The motivation for this starter was due to the fact that spring-boot had officially dropped support for spring-boot-starter-remote-shell for the 2.x versions.

This starter has been tested with spring-boot 2.0.0.M1 and 1.5.3.RELEASE. In theory however, it should work with any older releases.

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

The following are sample inputs/outputs from the shell command:

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
1) Currently, every method must return a String and take in exactly one String parameter.
2) Requires minimum JDK 8.
