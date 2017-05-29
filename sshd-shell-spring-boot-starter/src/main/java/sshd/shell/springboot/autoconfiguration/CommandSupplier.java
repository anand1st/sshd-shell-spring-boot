package sshd.shell.springboot.autoconfiguration;

/**
 *
 * @author anand
 */
@FunctionalInterface
interface CommandSupplier {
    
    String get(String arg) throws InterruptedException;
}
