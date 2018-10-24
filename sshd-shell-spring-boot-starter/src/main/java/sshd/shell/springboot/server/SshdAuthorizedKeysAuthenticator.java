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

import java.nio.file.Path;
import java.security.PublicKey;
import java.util.Collections;
import org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import sshd.shell.springboot.autoconfiguration.Constants;

/**
 *
 * @author anand
 */
class SshdAuthorizedKeysAuthenticator extends AuthorizedKeysAuthenticator {

    SshdAuthorizedKeysAuthenticator(Path path) {
        super(path);
    }

    @Override
    public boolean authenticate(String username, PublicKey key, ServerSession session) {
        if (super.authenticate(username, key, session)) {
            session.getIoSession().setAttribute(Constants.USER_ROLES, Collections.<String>singleton("*"));
            session.getIoSession().setAttribute(Constants.USER, username);
            return true;
        }
        return false;
    }
}
