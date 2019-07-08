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
package sshd.shell.springboot.server;

import java.util.Set;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import sshd.shell.springboot.autoconfiguration.Constants;
import sshd.shell.springboot.autoconfiguration.SshdShellProperties.Shell;

/**
 *
 * @author anand
 */
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)
class SimpleSshdPasswordAuthenticator implements PasswordAuthenticator {

    private final Shell props;
    private final Set<String> systemCommandRoles;

    @Override
    public boolean authenticate(String username, String password, ServerSession session) throws
            PasswordChangeRequiredException {
        if (username.equals(props.getUsername()) && password.equals(props.getPassword())) {
            session.getIoSession().setAttribute(Constants.USER_ROLES, systemCommandRoles);
            session.getIoSession().setAttribute(Constants.USER, username);
            return true;
        }
        return false;
    }
}
