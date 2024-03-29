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
package sshd.shell.springboot.autoconfiguration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

/**
 *
 * @author anand
 */
@SpringBootTest(classes = ConfigTest.class, properties = {
        "sshd.shell.auth.authType=AUTH_PROVIDER",
        "sshd.shell.username=bob",
        "sshd.shell.password=bob",
        "sshd.shell.auth.authProviderBeanName=authProvider",
        "spring.flyway.baseline-on-migrate=true",
        "spring.main.allow-circular-references=true"
})
@DirtiesContext
public class SshdShellAutoConfigurationAuthProviderInvalidAuthTypeTest extends
        SshdShellAutoConfigurationAuthProviderTest {
}
