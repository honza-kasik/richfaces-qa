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
package org.richfaces.tests.metamer.ftest.richNotifyMessages;

import org.openqa.selenium.TimeoutException;
import org.richfaces.fragment.common.Utils;
import org.richfaces.fragment.notify.NotifyMessage.NotifyMessagePosition;
import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
import org.richfaces.tests.metamer.ftest.extension.attributes.coverage.annotations.CoversAttributes;
import org.richfaces.tests.metamer.ftest.extension.configurator.skip.annotation.Skip;
import org.richfaces.tests.metamer.ftest.extension.configurator.templates.annotation.Templates;
import org.richfaces.tests.metamer.ftest.richNotify.TestNotifyAttributes;
import org.richfaces.tests.metamer.ftest.richNotifyMessage.NotifyMessageAttributes;
import org.richfaces.tests.metamer.ftest.webdriver.Attributes;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:jstefek@redhat.com">Jiri Stefek</a>
 */
public class TestNotifyMessagesAttributes extends AbstractNotifyMessagesTest {

    private final Attributes<NotifyMessagesAttributes> notifyMessagesAttributes = getAttributes();

    @Override
    public String getComponentTestPagePath() {
        return "richNotifyMessages/jsr303.xhtml";
    }

    @Test(groups = "smoke")
    @CoversAttributes("ajaxRendered")
    public void testAjaxRendered() {
        checkAjaxRendered();
    }

    @Test
    @CoversAttributes("dir")
    @IssueTracking("https://issues.jboss.org/browse/RF-12923")
    @Templates("plain")
    public void testDir() {
        checkDir();
    }

    @Test
    @CoversAttributes("escape")
    public void testEscape() {
        checkEscape();
    }

    @Test(groups = "smoke")
    @CoversAttributes("FOR")
    @Templates(exclude = { "richDataTable", "richCollapsibleSubTable", "richExtendedDataTable", "richDataGrid",
        "richList", "a4jRepeat", "hDataTable", "uiRepeat" })
    public void testFor() {
        checkFor(2);//2 messages
    }

    @Test(groups = "smoke")
    @CoversAttributes("FOR")
    @IssueTracking("https://issues.jboss.org/browse/RF-11298")
    @Templates(value = { "richDataTable", "richCollapsibleSubTable", "richExtendedDataTable", "richDataGrid",
        "richList", "a4jRepeat", "hDataTable", "uiRepeat" })
    public void testForInIterationComponents() {
        testFor();
    }

    @Test(groups = "smoke")
    @CoversAttributes("globalOnly")
    @Templates(exclude = { "richAccordion", "richCollapsiblePanel" })
    public void testGlobalOnly() {
        checkGlobalOnly(2);//2 messages
    }

    @Test
    @CoversAttributes("globalOnly")
    @Templates(value = { "richAccordion", "richCollapsiblePanel" })
    @IssueTracking("https://issues.jboss.org/browse/RF-11415")
    public void testGlobalOnlyInAccordionCollapsiblePanel() {
        testGlobalOnly();
    }

    @Test
    @CoversAttributes("lang")
    @IssueTracking("https://issues.jboss.org/browse/RF-12923")
    @Templates("plain")
    public void testLang() {
        checkLang();
    }

    @Test
    @IssueTracking("https://issues.jboss.org/browse/RF-11433")
    public void testMessagesTypes() {
        checkMessagesTypes();
    }

    @IssueTracking("https://issues.jboss.org/browse/RF-12925")
    @Test
    @Skip
    @CoversAttributes({ "showDetail", "showSummary" })
    public void testNoShowDetailNoShowSummary() {
        checkNoShowDetailNoShowSummary();
    }

    @Test
    @CoversAttributes("nonblocking")
    public void testNonblocking() {
        attsSetter()
            .setAttribute(NotifyMessagesAttributes.nonblocking).toValue(true)
            .setAttribute(NotifyMessagesAttributes.nonblockingOpacity).toValue(0)
            .asSingleAction().perform();
        generateValidationMessagesWithWait();
        Utils.triggerJQ(executor, "mouseover", getPage().getMessagesComponentWithGlobal().getItem(0).getRootElement());
        TestNotifyAttributes.waitForOpacityChange(0, getPage().getMessagesComponentWithGlobal().getItem(0).getRootElement());
        Utils.triggerJQ(executor, "mouseout", getPage().getMessagesComponentWithGlobal().getItem(0).getRootElement());
        TestNotifyAttributes.waitForOpacityChange(1, getPage().getMessagesComponentWithGlobal().getItem(0).getRootElement());
    }

    @Test
    @CoversAttributes("nonblockingOpacity")
    public void testNonblockingOpacity() {
        attsSetter()
            .setAttribute(NotifyMessagesAttributes.nonblocking).toValue(true)
            .setAttribute(NotifyMessagesAttributes.nonblockingOpacity).toValue(0.5)
            .asSingleAction().perform();
        generateValidationMessagesWithWait();
        Utils.triggerJQ(executor, "mouseover", getPage().getMessagesComponentWithGlobal().getItem(0).getRootElement());
        TestNotifyAttributes.waitForOpacityChange(0.5, getPage().getMessagesComponentWithGlobal().getItem(0).getRootElement());
        Utils.triggerJQ(executor, "mouseout", getPage().getMessagesComponentWithGlobal().getItem(0).getRootElement());
        TestNotifyAttributes.waitForOpacityChange(1, getPage().getMessagesComponentWithGlobal().getItem(0).getRootElement());
    }

    @Test
    @CoversAttributes("onclick")
    @Templates(value = "plain")
    public void testOnclick() {
        checkOnclick();
    }

    @Test
    @CoversAttributes("ondblclick")
    @Templates(value = "plain")
    public void testOndblclick() {
        checkOndblclick();
    }

    @Test
    @CoversAttributes("onkeydown")
    @Templates(value = "plain")
    public void testOnkeydown() {
        checkOnkeydown();
    }

    @Test
    @CoversAttributes("onkeypress")
    @Templates(value = "plain")
    public void testOnkeypress() {
        checkOnkeypress();
    }

    @Test
    @CoversAttributes("onkeyup")
    @Templates(value = "plain")
    public void testOnkeyup() {
        checkOnkeyup();
    }

    @Test
    @CoversAttributes("onmousedown")
    @Templates(value = "plain")
    public void testOnmousedown() {
        checkOnmousedown();
    }

    @Test
    @CoversAttributes("onmousemove")
    @Templates(value = "plain")
    public void testOnmousemove() {
        checkOnmousemove();
    }

    @Test
    @CoversAttributes("onmouseout")
    @Templates(value = "plain")
    public void testOnmouseout() {
        checkOnmouseout();
    }

    @Test
    @CoversAttributes("onmouseover")
    @Templates(value = "plain")
    public void testOnmouseover() {
        checkOnmouseover();
    }

    @Test
    @CoversAttributes("onmouseup")
    @Templates(value = "plain")
    public void testOnmouseup() {
        checkOnmouseup();
    }

    @Test
    @CoversAttributes("rendered")
    @Templates(value = "plain")
    public void testRendered() {
        checkRendered();
    }

    @Test
    @CoversAttributes("showCloseButton")
    public void testShowCloseButton() {
        notifyMessagesAttributes.set(NotifyMessagesAttributes.showCloseButton, Boolean.TRUE);
        generateValidationMessagesWithWait();
        int sizeBefore = getPage().getMessagesComponentWithGlobal().size();
        getPage().getMessagesComponentWithGlobal().getItem(0).close();
        Assert.assertEquals(getPage().getMessagesComponentWithGlobal().size(), sizeBefore - 1);

        notifyMessagesAttributes.set(NotifyMessagesAttributes.showCloseButton, Boolean.FALSE);
        generateValidationMessagesWithWait();
        try {
            getPage().getMessagesComponentWithGlobal().getItem(0).close();
        } catch (TimeoutException ok) {
            return;
        }
        Assert.fail("The notify message should not be closeable if there is no close button.");
    }

    @Test
    @CoversAttributes("showDetail")
    public void testShowDetail() {
        checkShowDetail();
    }

    @Test
    @CoversAttributes("showShadow")
    @IssueTracking("https://issues.jboss.org/browse/RF-13792")
    public void testShowShadow() {
        notifyMessagesAttributes.set(NotifyMessagesAttributes.showShadow, Boolean.TRUE);
        generateValidationMessagesWithWait();
        assertVisible(getPage().getMessagesComponentWithGlobal().getItem(0).advanced().getShadowElement(), "Shadow should be visible");

        notifyMessagesAttributes.set(NotifyMessagesAttributes.showShadow, Boolean.FALSE);
        generateValidationMessagesWithWait();
        assertNotVisible(getPage().getMessagesComponentWithGlobal().getItem(0).advanced().getShadowElement(), "Shadow should not be visible");
    }

    @Test
    @CoversAttributes("showSummary")
    public void testShowSummary() {
        checkShowSummary();
    }

    @Test
    @CoversAttributes("stack")
    public void testStack() {
        String[] stacks = { "topLeftStack", "bottomRightStack", "notRenderedStack" };
        //default position is top right
        notifyMessagesAttributes.set(NotifyMessagesAttributes.stack, "");
        generateValidationMessagesWithWait();
        Assert.assertEquals(getPage().getMessagesComponentWithGlobal().getItem(0).advanced().getPosition(), NotifyMessagePosition.TOP_RIGHT);

        notifyMessagesAttributes.set(NotifyMessagesAttributes.stack, stacks[0]);
        generateValidationMessagesWithWait();
        Assert.assertEquals(getPage().getMessagesComponentWithGlobal().getItem(0).advanced().getPosition(), NotifyMessagePosition.TOP_LEFT);

        notifyMessagesAttributes.set(NotifyMessagesAttributes.stack, stacks[1]);
        generateValidationMessagesWithWait();
        Assert.assertEquals(getPage().getMessagesComponentWithGlobal().getItem(0).advanced().getPosition(), NotifyMessagePosition.BOTTOM_RIGHT);

        notifyMessagesAttributes.set(NotifyMessagesAttributes.stack, stacks[2]);
        generateValidationMessagesWithWait();
        Assert.assertFalse(getPage().getMessagesComponentWithGlobal().advanced().isVisible());
        Assert.assertFalse(getPage().getMessagesComponentWithFor().advanced().isVisible());
    }

    @Test
    @CoversAttributes("stayTime")
    public void testStayTime() {
        attsSetter()
            .setAttribute(NotifyMessagesAttributes.stayTime).toValue(1000)
            .setAttribute(NotifyMessagesAttributes.sticky).toValue(false)
            .asSingleAction().perform();
        generateValidationMessagesWithWait();
        waiting(3000);
        Assert.assertEquals(getPage().getMessagesComponentWithGlobal().size(), 0, "There should be no message anymore.");
        Assert.assertEquals(getPage().getMessagesComponentWithFor().size(), 0, "There should be no message anymore.");
    }

    @Test
    @CoversAttributes("sticky")
    @IssueTracking("https://issues.jboss.org/browse/RF-11558")
    public void testSticky() {
        attsSetter()
            .setAttribute(NotifyMessagesAttributes.stayTime).toValue(1000)
            .setAttribute(NotifyMessagesAttributes.sticky).toValue(true)
            .setAttribute(NotifyMessageAttributes.showCloseButton).toValue(true)
            .asSingleAction().perform();
        generateValidationMessagesWithWait();
        waiting(3000);
        int sizeGlobal = getPage().getMessagesComponentWithGlobal().size();
        int sizeFor = getPage().getMessagesComponentWithFor().size();
        Assert.assertTrue(sizeGlobal > 0, "There should be some global messages.");
        Assert.assertTrue(sizeFor > 0, "There should be some messages with specified @for.");
        getPage().getMessagesComponentWithGlobal().getItem(0).close();
        Assert.assertEquals(getPage().getMessagesComponentWithGlobal().size(), sizeGlobal - 1, "There should be one global message less than before.");
        getPage().getMessagesComponentWithFor().getItem(0).close();
        Assert.assertEquals(getPage().getMessagesComponentWithFor().size(), sizeFor - 1, "There should be one message with specified @for less than before.");

        // when sticky, the close button should be always visible ( https://issues.jboss.org/browse/RF-11558 )
        notifyMessagesAttributes.set(NotifyMessagesAttributes.showCloseButton, false);
        generateValidationMessagesWithWait();
        waiting(3000);
        sizeGlobal = getPage().getMessagesComponentWithGlobal().size();
        sizeFor = getPage().getMessagesComponentWithFor().size();
        Assert.assertTrue(sizeGlobal > 0, "There should be some global messages.");
        Assert.assertTrue(sizeFor > 0, "There should be some messages with specified @for.");
        getPage().getMessagesComponentWithGlobal().getItem(0).close();
        Assert.assertEquals(getPage().getMessagesComponentWithGlobal().size(), sizeGlobal - 1, "There should be one global message less than before.");
        getPage().getMessagesComponentWithFor().getItem(0).close();
        Assert.assertEquals(getPage().getMessagesComponentWithFor().size(), sizeFor - 1, "There should be one message with specified @for less than before.");
    }

    @Test
    @IssueTracking({ "https://issues.jboss.org/browse/RF-12923", "https://issues.jboss.org/browse/RF-14164" })
    @CoversAttributes("style")
    @Templates(value = "plain")
    public void testStyle() {
        checkStyle();
    }

    @Test
    @CoversAttributes("styleClass")
    @Templates(value = "plain")
    public void testStyleClass() {
        checkStyleClass();
    }

    @Test
    @IssueTracking({ "https://issues.jboss.org/browse/RF-12923", "https://issues.jboss.org/browse/RF-14164" })
    @CoversAttributes("title")
    @Templates("plain")
    public void testTitle() {
        checkTitle();
    }

    @Override
    protected void waitingForValidationMessagesToShow() {
        submitWithA4jBtn();
    }
}
