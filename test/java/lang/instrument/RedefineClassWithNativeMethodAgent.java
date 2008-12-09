/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

import java.lang.instrument.*;
import java.net.*;
import java.util.*;
import java.io.*;

public class RedefineClassWithNativeMethodAgent {
    static Class clz;

    // just read the original class and redefine it via a Timer
    public static void premain(String agentArgs, final Instrumentation inst) throws Exception {
        String s = agentArgs.substring(0, agentArgs.indexOf(".class"));
        clz = Class.forName(s.replace('/', '.'));
        ClassLoader loader =
            RedefineClassWithNativeMethodAgent.class.getClassLoader();
        URL classURL = loader.getResource(agentArgs);
        if (classURL == null) {
            throw new Exception("Cannot find class: " + agentArgs);
        }

        int         redefineLength;
        InputStream redefineStream;

        System.out.println("Reading test class from " + classURL);
        if (classURL.getProtocol().equals("file")) {
            File f = new File(classURL.getFile());
            redefineStream = new FileInputStream(f);
            redefineLength = (int) f.length();
        } else {
            URLConnection conn = classURL.openConnection();
            redefineStream = conn.getInputStream();
            redefineLength = conn.getContentLength();
        }

        final byte[] buffer = new byte[redefineLength];
        new BufferedInputStream(redefineStream).read(buffer);
        new Timer(true).schedule(new TimerTask() {
            public void run() {
                try {
                    System.out.println("Instrumenting");
                    ClassDefinition cld = new ClassDefinition(clz, buffer);
                    inst.redefineClasses(new ClassDefinition[] { cld });
                }
                catch (Exception e) { e.printStackTrace(); }
            }
        }, 500);
    }
}
