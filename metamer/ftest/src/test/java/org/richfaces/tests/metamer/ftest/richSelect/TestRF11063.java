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
package org.richfaces.tests.metamer.ftest.richSelect;

import static org.testng.Assert.assertEquals;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.richfaces.tests.metamer.ftest.AbstractWebDriverTest;
import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:jstefek@redhat.com">Jiri Stefek</a>
 */
public class TestRF11063 extends AbstractWebDriverTest {

    @FindBy(css = "input[id$=':hInputText']")
    private WebElement hInputText;
    @FindBy(css = "[id$='select'] .rf-sel-inp")
    private WebElement selectInput;

    @Override
    public String getComponentTestPagePath() {
        return "richSelect/rf-11063.xhtml";
    }

    @Test
    @IssueTracking("https://issues.jboss.org/browse/RF-11063")
    public void testSelectIsAlignedVerticallyAsHInputText() {
        final int tolerance = 8;// px
        // check the top left location of inputs
        assertEquals(selectInput.getLocation().y, hInputText.getLocation().y, tolerance);
    }
}
