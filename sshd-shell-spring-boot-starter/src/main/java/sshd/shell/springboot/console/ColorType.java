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

import org.jline.utils.AttributedStyle;

/**
 *
 * @author anand
 */
@lombok.AllArgsConstructor
public enum ColorType {

    BLACK(AttributedStyle.BLACK),
    RED(AttributedStyle.RED),
    GREEN(AttributedStyle.GREEN),
    YELLOW(AttributedStyle.YELLOW),
    BLUE(AttributedStyle.BLUE),
    MAGENTA(AttributedStyle.MAGENTA),
    CYAN(AttributedStyle.CYAN),
    WHITE(AttributedStyle.WHITE);

    public final int value;
}
