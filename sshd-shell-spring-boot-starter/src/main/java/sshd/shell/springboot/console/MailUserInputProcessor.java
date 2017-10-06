/*
 * Copyright 2017 anand.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sshd.shell.springboot.console;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 *
 * @author anand
 */
@Component
@Order(2)
@ConditionalOnProperty(prefix = "spring.mail", name = {"host", "port", "username", "password"})
@ConditionalOnClass(JavaMailSender.class)
@lombok.extern.slf4j.Slf4j
class MailUserInputProcessor extends BaseUserInputProcessor {

    private final Pattern pattern = Pattern.compile("[\\w\\W]+\\s?\\|\\s?m (.+)");
    @Autowired
    private JavaMailSender mailSender;

    @Override
    public Optional<UsageInfo> getUsageInfo() {
        return Optional.of(new UsageInfo(Arrays.<UsageInfo.Row>asList(
                new UsageInfo.Row("m <emailId>", "Send response output of command execution to <emailId>"),
                new UsageInfo.Row("", "Example usage: help | m bob@hope.com"))));
    }

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public void processUserInput(String userInput) throws InterruptedException, ShellException {
        String[] part = splitAndValidateCommand(userInput, "\\|", 2);
        Matcher matcher = pattern.matcher(userInput);
        Assert.isTrue(matcher.find(), "Unexpected error");
        String emailId = matcher.group(1).trim();
        String output = processCommands(part[0]);
        sendMail(emailId, part[0], output);
    }

    private void sendMail(String emailId, String command, String output) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        try {
            helper.setTo(emailId);
            helper.setSubject(command);
            helper.setText(output);
            mailSender.send(mimeMessage);
            ConsoleIO.writeOutput("Output response sent to " + emailId);
        } catch (MessagingException | MailException ex) {
            ConsoleIO.writeOutput("Error sending mail, please check logs for more info");
            log.error("Error sending mail", ex);
        }
    }
}
