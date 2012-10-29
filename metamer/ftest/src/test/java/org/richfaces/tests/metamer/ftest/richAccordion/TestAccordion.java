/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.richfaces.tests.metamer.ftest.richAccordion;

import java.net.URL;
import javax.faces.event.PhaseId;
import static org.jboss.arquillian.ajocado.utils.URLUtils.buildUrl;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.spi.annotations.Page;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.richfaces.tests.metamer.ftest.AbstractWebDriverTest;
import org.richfaces.tests.metamer.ftest.BasicAttributes;
import org.richfaces.tests.metamer.ftest.annotations.RegressionTest;
import static org.richfaces.tests.metamer.ftest.webdriver.AttributeList.accordionAttributes;
import org.richfaces.tests.page.fragments.impl.accordion.AccordionItem;
import org.richfaces.tests.page.fragments.impl.accordion.AccordionItemImpl;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:jpapouse@redhat.com">Jan Papousek</a>
 */
public class TestAccordion extends AbstractWebDriverTest<AccordionPage> {

    @Page
    private AccordionPage page;

    @Override
    protected AccordionPage createPage() {
        return page;
    }

    @Override
    public URL getTestUrl() {
        return buildUrl(contextPath, "faces/components/richAccordion/simple.xhtml");
    }

    @Test
    public void testInit() {
        assertTrue(getPage().isAccordionVisible(), "Accordion is not present on the page.");
        assertEquals(getPage().getAccordion().size(), 5, "number of visible headers");
        assertTrue(getPage().getAccordion().getItem(0).isActive(), "The first accordion item should be active.");
        for (int i = 1; i < getPage().getAccordion().size(); i++) {
            assertTrue(getPage().getAccordion().getItem(i).isInactive(), "Item" + (i + 1) + " shouldn't be active.");
        }
    }

    @Test
    public void testActiveItem() {
        accordionAttributes.set(AccordionAttributes.activeItem, "item5");

        assertTrue(getPage().isAccordionVisible(), "Accordion is not present on the page.");
        for (AccordionItem item: getPage().getAccordion()) {
            assertTrue(item.isActive() || item.isInactive() || !item.isEnabled(), "Item" + item.getHeader() + "'s header should be visible.");
        }

        assertTrue(getPage().getAccordion().getItem(4).isActive(), "Content of item5 should be visible.");
        for (int i = 0; i < 4; i++) {
            assertTrue(getPage().getAccordion().getItem(i).isInactive(), "Item" + (i + 1) + " shouldn't be active.");
        }

        accordionAttributes.set(AccordionAttributes.activeItem, "item4");
        for (AccordionItem item: getPage().getAccordion()) {
            assertTrue(item.isActive() || item.isInactive() || !item.isEnabled(), "Item" + item.getHeader() + "'s header should be visible.");
        }

        for (AccordionItem item: getPage().getAccordion()) {
            assertTrue(item.isInactive(), "Item" + item.getHeader() + " shouldn't be active.");
        }
    }

    @Test
    public void testCycledSwitching() {
        String accordionId = ((JavascriptExecutor) driver).executeScript("return testedComponentId").toString();
        Object result = null;

        // RichFaces.$('form:accordion').nextItem('item4') will be null
        result = ((JavascriptExecutor) driver).executeScript("return RichFaces.$('" + accordionId
            + "').nextItem('item4')");
        assertEquals(result, null, "Result of function nextItem('item4')");

        // RichFaces.$('form:accordion').prevItem('item1') will be null
        result = ((JavascriptExecutor) driver).executeScript("return RichFaces.$('" + accordionId
            + "').prevItem('item1')");
        assertEquals(result, null, "Result of function prevItem('item1')");

        accordionAttributes.set(AccordionAttributes.cycledSwitching, true);

        // RichFaces.$('form:accordion').nextItem('item5') will be item1
        result = ((JavascriptExecutor) driver).executeScript("return RichFaces.$('" + accordionId
            + "').nextItem('item5')");
        assertEquals(result.toString(), "item1", "Result of function nextItem('item5')");

        // RichFaces.$('form:accordion').prevItem('item1') will be item5
        result = ((JavascriptExecutor) driver).executeScript("return RichFaces.$('" + accordionId
            + "').prevItem('item1')");
        assertEquals(result.toString(), "item5", "Result of function prevItem('item1')");
    }

    @Test
    public void testDir() {
        testDir(getPage().getAccordionRootElement());
    }

    @Test
    public void testHeight() {
        WebElement accordionRoot = getPage().getAccordionRootElement();
        // height = null
        assertEquals(accordionRoot.getAttribute("style"), "", "Attribute style should not be present.");

        // height = 300px
        accordionAttributes.set(AccordionAttributes.height, "300px");
        assertEquals(accordionRoot.getCssValue("height"), "300px", "Attribute height");
    }

    @Test
    public void testImmediate() {
        accordionAttributes.set(AccordionAttributes.immediate, true);

        getPage().getAccordion().getItem(2).activate();

        phaseInfo.assertPhases(PhaseId.RESTORE_VIEW, PhaseId.APPLY_REQUEST_VALUES, PhaseId.RENDER_RESPONSE);
        phaseInfo.assertListener(PhaseId.APPLY_REQUEST_VALUES, "item changed: item1 -> item3");
    }

    @Test
    public void testItemActiveHeaderClass() {
        testStyleClass(((AccordionItemImpl) getPage().getAccordion().getActiveItem()).getHeaderElement(), BasicAttributes.itemActiveHeaderClass);
    }

    @Test
    public void testItemChangeListener() {
        getPage().getAccordion().getItem(2).activate();
        phaseInfo.assertListener(PhaseId.UPDATE_MODEL_VALUES, "item changed: item1 -> item3");
    }

    @Test
    public void testItemContentClass() {
        for (AccordionItem item: getPage().getAccordion()) {
            testStyleClass(((AccordionItemImpl) item).getContentElement(), BasicAttributes.itemContentClass);
        }
    }

    @Test
    public void testItemDisabledHeaderClass() {
        for (AccordionItem item: getPage().getAccordion()) {
            if (!item.isEnabled()) {
                testStyleClass(((AccordionItemImpl) item).getHeaderElement(), BasicAttributes.itemDisabledHeaderClass);
            }
        }
    }

    @Test
    public void testItemHeaderClass() {
        for (AccordionItem item: getPage().getAccordion()) {
            testStyleClass(((AccordionItemImpl) item).getToActivateElement(), BasicAttributes.itemHeaderClass);
        }
    }

    @Test
    public void testItemchangeEvents() {
        accordionAttributes.set(AccordionAttributes.onbeforeitemchange, "metamerEvents += \"beforeitemchange \"");
        accordionAttributes.set(AccordionAttributes.onitemchange, "metamerEvents += \"itemchange \"");

        executeJS("metamerEvents = \"\";");
        Graphene.guardXhr(getPage().getAccordion().getItem(2)).activate();
        String[] events = ((String) executeJS("return metamerEvents;")).split(" ");

        assertEquals(events[0], "beforeitemchange", "Attribute onbeforeitemchange doesn't work");
        assertEquals(events[1], "itemchange", "Attribute onbeforeitemchange doesn't work");
    }

    @Test
    public void testItemInactiveHeaderClass() {
        for (AccordionItem item: getPage().getAccordion()) {
            if (item.isInactive() && item.isEnabled()) {
                testStyleClass(((AccordionItemImpl) item).getHeaderElement(), BasicAttributes.itemInactiveHeaderClass);
            }
        }
    }

    @Test
    public void testLang() {
        testHTMLAttribute(getPage().getAccordionRootElement(), accordionAttributes, AccordionAttributes.lang, "sk");
    }

    @Test
    public void testOnbeforeitemchange() {
        Action action = new Action() {
            @Override
            public void perform() {
                getPage().getAccordion().getItem(1).activate();
            }
        };
        testFireEvent(accordionAttributes, AccordionAttributes.onbeforeitemchange, action);
    }

    @Test
    public void testOnclick() {
        Action action = new Actions(driver).click(getPage().getAccordionRootElement()).build();
        testFireEvent(accordionAttributes, AccordionAttributes.onclick, action);
    }

    @Test
    public void testOndblclick() {
        Action action = new Actions(driver).doubleClick(getPage().getAccordionRootElement()).build();
        testFireEvent(accordionAttributes, AccordionAttributes.ondblclick, action);
    }

    @Test
    public void testOnitemchange() {
        Action action = new Action() {
            @Override
            public void perform() {
                getPage().getAccordion().getItem(1).activate();
            }
        };
        testFireEvent(accordionAttributes, AccordionAttributes.onitemchange, action);
    }

    @Test
    public void testOnmousedown() {
        Action action = new Actions(driver).clickAndHold(getPage().getAccordionRootElement()).build();
        testFireEvent(accordionAttributes, AccordionAttributes.onmousedown, action);
    }

    @Test
    public void testOnmousemove() {
        Action action = new Actions(driver).moveToElement(getPage().getAccordionRootElement()).build();
        testFireEvent(accordionAttributes, AccordionAttributes.onmousemove, action);
    }

    @Test
    public void testOnmouseout() {
        testFireEventWithJS(getPage().getAccordionRootElement(), accordionAttributes, AccordionAttributes.onmouseout);
    }

    @Test
    public void testOnmouseover() {
        Action action = new Actions(driver).moveToElement(getPage().getAccordionRootElement()).build();
        testFireEvent(accordionAttributes, AccordionAttributes.onmouseover, action);
    }

    @Test
    public void testOnmouseup() {
        Action action = new Actions(driver).click(getPage().getAccordionRootElement()).build();
        testFireEvent(accordionAttributes, AccordionAttributes.onmouseup, action);
    }

    @Test
    public void testRendered() {
        accordionAttributes.set(AccordionAttributes.rendered, false);
        assertFalse(getPage().isAccordionVisible());
    }

    @Test
    public void testSimple() {
        for (AccordionItem item: getPage().getAccordion()) {
            if (item.isEnabled()) {
                item.activate();
                Assert.assertTrue(item.isActive());
            }
        }
    }

    @Test
    public void testStyle() {
        testStyle(getPage().getAccordionRootElement());
    }

    @Test
    public void testStyleClass() {
        testStyleClass(getPage().getAccordionRootElement());
    }

    @Test
    @RegressionTest("https://issues.jboss.org/browse/RF-12532")
    public void testSwitchTypeNull() {
        for (int i = 2; i >= 0; i--) {
            Graphene.guardXhr(getPage().getAccordion().getItem(i)).activate();
        }
    }

    @Test
    @RegressionTest("https://issues.jboss.org/browse/RF-12532")
    public void testSwitchTypeAjax() {
        accordionAttributes.set(AccordionAttributes.switchType, "ajax");
        testSwitchTypeNull();
    }

    @Test
    @RegressionTest("https://issues.jboss.org/browse/RF-12532")
    public void testSwitchTypeClient() {
        accordionAttributes.set(AccordionAttributes.switchType, "client");
        for (int i = 2; i >= 0; i--) {
            Graphene.guardNoRequest(getPage().getAccordion().getItem(i)).activate();
        }
    }

    @Test
    @RegressionTest({ "https://issues.jboss.org/browse/RF-10040", "https://issues.jboss.org/browse/RF-12532" })
    public void testSwitchTypeServer() {
        accordionAttributes.set(AccordionAttributes.switchType, "server");
        for (int i = 2; i >= 0; i--) {
            Graphene.guardHttp(getPage().getAccordion().getItem(i)).activate();
        }
    }

}
