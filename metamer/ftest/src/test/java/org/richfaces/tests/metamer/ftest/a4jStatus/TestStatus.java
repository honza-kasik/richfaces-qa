/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010-2016, Red Hat, Inc. and individual contributors
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
 */
package org.richfaces.tests.metamer.ftest.a4jStatus;

import java.util.concurrent.TimeUnit;

import org.richfaces.tests.metamer.ftest.MetamerAttributes;
import org.richfaces.tests.metamer.ftest.extension.attributes.coverage.annotations.CoversAttributes;
import org.richfaces.tests.metamer.ftest.extension.configurator.templates.annotation.Templates;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
 * @author <a href="mailto:jstefek@redhat.com">Jiri Stefek</a>
 */
public class TestStatus extends AbstractStatusTest {

    @Override
    public String getComponentTestPagePath() {
        return "a4jStatus/facets.xhtml";
    }

    @Test(groups = "smoke")
    public void testRequestButton1() {
        checkStatus(getButton1(), "START", "STOP");
    }

    @Test(groups = "smoke")
    public void testRequestButton2() {
        checkStatus(getButton2(), "START", "STOP");
    }

    @Test(groups = "smoke")
    public void testRequestButtonError() {
        checkStatus(getButtonError(), "START", "ERROR");
    }

    @Test
    public void testInterleaving() {
        testRequestButton1();
        testRequestButtonError();
        testRequestButton2();
        testRequestButtonError();
        testRequestButton1();
    }

    @Test
    @CoversAttributes("rendered")
    @Templates(value = "plain")
    public void testRendered() {
        assertPresent(getStatus().advanced().getRootElement(), "Status should be present");

        getStatusAttributes().set(StatusAttributes.rendered, Boolean.FALSE);
        assertNotPresent(getStatus().advanced().getRootElement(), "Status should not be present");

        // check default status works
        getMetamerAttributes().set(MetamerAttributes.metamerResponseDelay, DELAY);
        getButton1().click();
        getMetamerStatus().advanced().waitUntilStatusTextChanges("WORKING")
            .withTimeout(WAIT_TIME_STATUS_CHANGES, TimeUnit.MILLISECONDS).perform();
        getMetamerStatus().advanced().waitUntilStatusTextChanges("")
            .withTimeout(WAIT_TIME_STATUS_CHANGES, TimeUnit.MILLISECONDS).perform();
    }
}
