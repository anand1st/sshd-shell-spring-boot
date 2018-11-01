/*
 * Copyright 2018 anand.
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
package sshd.shell.springboot.command;

import java.io.IOException;
import org.slf4j.Logger;

/**
 *
 * @author anand
 */
public enum CommandUtils {
    ;
        
    public static String process(Logger log, JsonProcessor processor) {
        try {
            return processor.process();
        } catch (IOException | IllegalArgumentException ex) {
            log.warn("Invalid json", ex);
            return "Expected valid json as argument";
        }
    }

    @FunctionalInterface
    public static interface JsonProcessor {

        String process() throws IOException;
    }
}
