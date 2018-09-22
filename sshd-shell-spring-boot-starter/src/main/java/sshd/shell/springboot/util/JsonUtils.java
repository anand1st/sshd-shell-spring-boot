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
package sshd.shell.springboot.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;

/**
 *
 * @author anand
 */
@lombok.extern.slf4j.Slf4j
public enum JsonUtils {
    ;
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final ObjectWriter WRITER = OBJECT_MAPPER.writer().withDefaultPrettyPrinter();
    
    public static String asJson(Object object) {
        try {
            return WRITER.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            log.error("Error processing json output", ex);
            return "Error processing json output: " + ex.getMessage();
        }
    }

    public static <E> E stringToObject(String json, Class<E> clazz) throws IOException {
        return OBJECT_MAPPER.readValue(json, clazz);
    }
}
