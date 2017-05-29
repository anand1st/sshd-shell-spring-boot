package sshd.shell.springboot.autoconfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author anand
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface SshdShellCommand {
    
    /**
     * Command.
     * @return 
     */
    public String value();
    
    /**
     * Description of command.
     * @return 
     */
    public String description();
}
