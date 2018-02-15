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

import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author anand
 */
public class ConsoleIOTest {
    
    @Test
    public void testConsoleIOAsJsonException() {
        assertTrue(ConsoleIO.asJson(new X("x")).startsWith("Error processing json output"));
    }
    
    @lombok.AllArgsConstructor
    private static class X {
        final String x;
    }
}
