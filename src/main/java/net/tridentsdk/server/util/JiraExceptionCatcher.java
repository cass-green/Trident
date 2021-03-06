/*
 * Trident - A Multithreaded Server Alternative
 * Copyright 2017 The TridentSDK Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tridentsdk.server.util;

import net.tridentsdk.Server;

import java.io.PrintStream;

/**
 * Catches exceptions in the server process that are
 * unrecoverable and pastes a link for creating an issue
 * with details pulled from the environment.
 */
public final class JiraExceptionCatcher {
    // Prevent instantiation
    private JiraExceptionCatcher() {
    }

    /**
     * Catches the given exception and reformats it,
     * pulling
     * in the stacktrace information and providing a post
     * link to the TridentSDK JIRA.
     *
     * @param e the exception to catch
     */
    public static void serverException(Exception e) {
        String url = "https://github.com/TridentSDK/Trident/issues/new";

        String environment = "Trident Version: " + Server.VERSION + "\n" +
                "Operating System: " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ")\n" +
                "System Architecture: " + System.getProperty("os.arch") + "\n" +
                "Java Version: " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")";

        PrintStream o = System.err;
        o.println();
        o.println("Unhandled Exception occurred while starting the server.");
        o.println("This was not intended to happen.");
        o.println("Please report this at " + url);
        o.println();
        o.println("SYSTEM INFO:");
        o.println("============");
        o.println(environment);
        o.println();
        o.println("STACKTRACE");
        o.println("==========");
        e.printStackTrace();
    }
}