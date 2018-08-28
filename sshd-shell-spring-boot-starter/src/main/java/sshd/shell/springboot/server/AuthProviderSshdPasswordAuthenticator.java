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

import java.util.stream.Collectors;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import sshd.shell.springboot.autoconfiguration.Constants;

/**
 *
 * @author anand
 */
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)
@lombok.extern.slf4j.Slf4j
class AuthProviderSshdPasswordAuthenticator implements PasswordAuthenticator {

    private final AuthenticationProvider authProvider;

    @Override
    public boolean authenticate(String username, String password, ServerSession session) throws
            PasswordChangeRequiredException {
        try {
            Authentication auth = authProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            session.getIoSession().setAttribute(Constants.USER, username);
            session.getIoSession().setAttribute(Constants.USER_ROLES, auth.getAuthorities().stream()
                    .map(ga -> ga.getAuthority()).collect(Collectors.toSet()));
            return true;
        } catch (AuthenticationException ex) {
            log.warn(ex.getMessage());
            return false;
        }
    }
}
