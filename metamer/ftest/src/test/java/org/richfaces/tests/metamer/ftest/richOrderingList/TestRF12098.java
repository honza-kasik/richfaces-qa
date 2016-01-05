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
package org.richfaces.tests.metamer.ftest.richOrderingList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.richfaces.fragment.common.picker.ChoicePickerHelper;
import org.richfaces.fragment.message.RichFacesMessage;
import org.richfaces.fragment.orderingList.RichFacesOrderingList;
import org.richfaces.tests.metamer.ftest.AbstractWebDriverTest;
import org.richfaces.tests.metamer.ftest.annotations.RegressionTest;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:jstefek@redhat.com">Jiri Stefek</a>
 */
public class TestRF12098 extends AbstractWebDriverTest {

    @FindBy(css = "[id$=list]")
    private RichFacesOrderingList list;
    @FindBy(css = "[id$=submitButton]")
    private WebElement submitButton;
    @FindBy(className = "rf-msg")
    RichFacesMessage msg;

    @Override
    public String getComponentTestPagePath() {
        return "richOrderingList/rf-12098.xhtml";
    }

    @Test
    @RegressionTest("https://issues.jboss.org/browse/RF-12098")
    public void testListCanHandleItemsWithCommas() {
        String listElementsBefore = list.advanced().getContentAreaElement().getText();
        // reorganize elements in list
        list
            .select(0).putItAfter(ChoicePickerHelper.byIndex().last())
            .select(3).putItBefore(0)
            .select(3).putItAfter(4);
        String listElementsBeforeSubmit = list.advanced().getContentAreaElement().getText();

        // check elements were reorganized
        assertNotEquals(listElementsBefore, listElementsBeforeSubmit);

        // submit
        Graphene.guardHttp(submitButton).click();

        // check no error message is displayed
        assertFalse(msg.advanced().isVisible());

        String listElemenstAfterSubmit = list.advanced().getContentAreaElement().getText();
        // check elements stay reorganized
        assertEquals(listElemenstAfterSubmit, listElementsBeforeSubmit);
    }
}
