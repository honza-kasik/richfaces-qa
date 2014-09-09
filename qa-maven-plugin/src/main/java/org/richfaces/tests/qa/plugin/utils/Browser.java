/*******************************************************************************
 * JBoss, Home of Professional Open Source
 * Copyright 2010-2014, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *******************************************************************************/
package org.richfaces.tests.qa.plugin.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:jstefek@redhat.com">Jiri Stefek</a>
 */
public class Browser {

    private static final Integer UNKNOWN_VERSION = -1;
    private final BrowserName name;
    private Integer version;

    public Browser(String name, Integer version) {
        this.name = BrowserName.getNameFor(name);
        this.version = version;
    }

    public Browser(BrowserName name, Integer version) {
        this.name = name;
        this.version = version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public static Browser parseFromString(String name) {
        if (!name.matches("([a-zA-Z])+(\\d{1})*")) {
            return new Browser("unknown", UNKNOWN_VERSION);
        }
        String browser = name.split("\\d{1}")[0];
        String version = name.replace(browser, "");
        if (version == null || version.isEmpty()) {
            return new Browser(browser, UNKNOWN_VERSION);
        }
        return new Browser(browser, Integer.parseInt(version));
    }

    public BrowserName getName() {
        return name;
    }

    public Integer getVersion() {
        return version;
    }

    public boolean isBrowser(BrowserName bn) {
        return bn.equals(this.name);
    }

    public boolean isChrome() {
        return isBrowser(BrowserName.chrome);
    }

    public boolean isFirefox() {
        return isBrowser(BrowserName.firefox);
    }

    public boolean isInternetExplorer() {
        return isBrowser(BrowserName.internetExplorer);
    }

    public boolean isUnknown() {
        return isBrowser(BrowserName.unknown);
    }

    public boolean isUnknownVersion() {
        return getVersion().equals(UNKNOWN_VERSION);
    }

    @Override
    public String toString() {
        return getName() + getVersion().toString();
    }

    public static enum BrowserName {

        firefox("ff", "firefox"), internetExplorer("internetExplorer", "explorer", "ie"), chrome("chrome", "cr"), unknown;

        private final Set<String> aliases;

        private BrowserName(String... aliases) {
            this.aliases = new HashSet<String>();
            for (String alias : aliases) {
                this.aliases.add(alias.toLowerCase());
            }
        }

        public static BrowserName getNameFor(String name) {
            String lowerCaseName = name.toLowerCase();
            for (BrowserName bn : values()) {
                if (bn.getAliases().contains(lowerCaseName)) {
                    return bn;
                }
            }
            return unknown;
        }

        public Set<String> getAliases() {
            return Collections.unmodifiableSet(aliases);
        }
    }
}
