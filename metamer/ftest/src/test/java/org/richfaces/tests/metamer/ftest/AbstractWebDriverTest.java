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
package org.richfaces.tests.metamer.ftest;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.condition.element.WebElementConditionFactory;
import org.jboss.arquillian.graphene.javascript.JavaScript;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.richfaces.component.Positioning;
import org.richfaces.component.SwitchType;
import org.richfaces.fragment.common.AdvancedVisibleComponentIteractions;
import org.richfaces.fragment.common.Event;
import org.richfaces.fragment.common.Locations;
import org.richfaces.fragment.common.TextInputComponentImpl;
import org.richfaces.fragment.common.Utils;
import org.richfaces.fragment.common.VisibleComponent;
import org.richfaces.fragment.popupPanel.TextualRichFacesPopupPanel;
import org.richfaces.tests.configurator.unstable.UnstableTestConfigurator;
import org.richfaces.tests.metamer.Template;
import org.richfaces.tests.metamer.ftest.attributes.AttributeEnum;
import org.richfaces.tests.metamer.ftest.extension.configurator.Configurator;
import org.richfaces.tests.metamer.ftest.extension.configurator.config.Config;
import org.richfaces.tests.metamer.ftest.extension.configurator.transformer.DataProviderTestTransformer;
import org.richfaces.tests.metamer.ftest.extension.multipleEventFiring.MultipleEventFirerer;
import org.richfaces.tests.metamer.ftest.extension.multipleEventFiring.MultipleEventFirererImpl;
import org.richfaces.tests.metamer.ftest.extension.tester.attributes.AttributeNotSetException;
import org.richfaces.tests.metamer.ftest.extension.tester.attributes.AttributesHandler;
import org.richfaces.tests.metamer.ftest.extension.tester.attributes.MultipleAttributesSetter;
import org.richfaces.tests.metamer.ftest.extension.tester.basic.TestResourcesProvider;
import org.richfaces.tests.metamer.ftest.webdriver.Attributes;
import org.richfaces.tests.metamer.ftest.webdriver.AttributesImpl;
import org.richfaces.tests.metamer.ftest.webdriver.MetamerPage;
import org.richfaces.tests.metamer.ftest.webdriver.MetamerPage.WaitRequestType;
import org.richfaces.tests.metamer.ftest.webdriver.UnsafeAttributes;
import org.richfaces.tests.metamer.ftest.webdriver.utils.MetamerJavascriptUtils;
import org.richfaces.tests.metamer.ftest.webdriver.utils.StopWatch;
import org.richfaces.tests.metamer.ftest.webdriver.utils.StringEqualsWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IHookCallBack;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.text.MessageFormat.format;
import static org.jboss.test.selenium.support.url.URLUtils.buildUrl;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public abstract class AbstractWebDriverTest extends AbstractMetamerTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractWebDriverTest.class);

    protected static final int WAIT_TIME = 5;// s
    protected static final int MINOR_WAIT_TIME = 50;// ms
    protected static final int TRIES = 20;// for guardListSize and expectedReturnJS
    private static final String FACES_COMPONENTS_PATH = "faces/components/";

    @Drone
    protected WebDriver driver;

    @ArquillianResource
    protected JavascriptExecutor executor;

    @JavaScript
    protected MetamerJavascriptUtils jsUtils;

    @JavaScript
    protected JSErrorStorage jsErrorStorage;

    @FindBy(css = "input[id$=statusInput]")
    protected TextInputComponentImpl statusInput;

    @FindBy(css = "[id$=containerPopupPanel]")
    protected TextualRichFacesPopupPanel popupTemplate;

    @Page
    private MetamerPage metamerPage;

    protected DriverType driverType;

    private final Boolean[] booleans = { false, true };

    private Positioning positioning;// used for testJointPoint, testDirection
    protected static final EnumSet<Positioning> STRICT_POSITIONING = EnumSet.of(Positioning.bottomLeft, Positioning.bottomRight, Positioning.topLeft, Positioning.topRight);

    // this field is used by MetamerTestInfo to gather information about actual test method configuration
    private Config currentConfiguration;

    // field for reporting purposes, please *DO NOT* change its name since reflection is used to access it
    private Map<String, List<Config>> configurations = new HashMap<>();

    /**
     * @return method should return a String representing 'component/page' (case sensitive). E.g.:
     * <code>a4jActionListener/all.xhtml</code>, <code>a4jAjax/hCommandButton.xhtml</code>
     */
    public abstract String getComponentTestPagePath();

    @Override
    public URL getTestUrl() {
        return buildUrl(contextPath, FACES_COMPONENTS_PATH + getComponentTestPagePath());
    }

    protected void blur(WaitRequestType g) {
        getMetamerPage().blur(g);
    }

    public enum DriverType {

        FireFox(FirefoxDriver.class),
        InternetExplorer(InternetExplorerDriver.class),
        Chrome(ChromeDriver.class);

        private final Class<?> clazz;

        private DriverType(Class<?> clazz) {
            this.clazz = clazz;
        }

        public static DriverType getCurrentType(WebDriver wd) {
            for (DriverType type : values()) {
                if (type.clazz.isInstance(wd)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown Driver");
        }
    }

    private final Configurator c = new Configurator();

    @DataProvider(name = DataProviderTestTransformer.DATAPROVIDER_NAME)
    public Object[][] provide(Method m) {
        Object[][] configs = c.prepareConfigurationsForMethod(m, this);
        List<List<Config>> archiveConfigs = c.getConfigurations();
        configurations.put(m.getName(), archiveConfigs == null || archiveConfigs.isEmpty() ?
                Collections.emptyList() :
                new ArrayList<>(archiveConfigs.get(0)));
        log.debug("Set configurations to '{}'", configurations);
        return configs;
    }

    @BeforeMethod(alwaysRun = true)
    public void configure() {
        currentConfiguration = c.configureNextStep();
    }

    /**
     * Overriding method from Arquillian to introduce new test execution behavior
     */
    @Override
    public void run(final IHookCallBack callBack, final ITestResult testResult) {
        super.run(UnstableTestConfigurator.getGuardedCallback(callBack), testResult);
    }

    @AfterMethod(alwaysRun = true)
    public void unconfigure() {
        if (currentConfiguration != null) {
            currentConfiguration.unconfigure();
        }
    }

    /**
     * Opens the tested page. If templates is not empty nor null, it appends url parameter with templates.
     */
    @BeforeMethod(alwaysRun = true, dependsOnMethods = "configure")
    public void loadPage() {
        if (driver == null) {
            throw new SkipException("webDriver isn't initialized");
        }
        // delete session
        driver.manage().deleteAllCookies();
        // move mouse to upper left corner of window so it will not stay over tested component
        moveMouseToUpperLeftCorner();
        // try to load the page
        for (int i = 0; i < 3; i++) {
            openPageWithCurrentConfiguration();
            // check page is correctlu loaded or repeat
            if (checkPageIsLoadedCorrectly()) {
                break;
            }
        }
        driverType = DriverType.getCurrentType(driver);
        // resize browser window to 1280x1024 or full screen
        driver.manage().window().setSize(new Dimension(1920, 1080));
    }

    protected void openPageWithCurrentConfiguration() {
        if (runInPortalEnv) {
            goToTestInPortal();
        } else {
            driver.get(buildUrl(getTestUrl() + "?templates=" + template.toString()).toExternalForm());
        }
    }

    /**
     * During opening of a page with templates set up, one can face IndexOutOfBoundsException.
     */
    private boolean checkPageIsLoadedCorrectly() {
        String pageSource = driver.getPageSource();
        return !pageSource.contains("java.lang.IndexOutOfBoundsException");
    }

    protected boolean isInPopupTemplate() {
        return template.contains(Template.RICHPOPUPPANEL);
    }

    protected void waitUtilNoTimeoutsArePresent() {
        Graphene.waitAjax().pollingEvery(200, TimeUnit.MILLISECONDS).until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver t) {
                return Boolean.parseBoolean(String.valueOf(executeJS("return window.areNoTimeoutsPresent();")));
            }
        });
    }

    private void moveMouseToUpperLeftCorner() {
        new Actions(driver).moveToElement(driver.findElement(Utils.BY_BODY), 0, 0).perform();
    }

    protected Attributes<BasicAttributes> getBasicAttributes() {
        return getAttributes();
    }

    protected Attributes<MetamerAttributes> getMetamerAttributes() {
        return getAttributes();
    }

    protected MetamerPage getMetamerPage() {
        return metamerPage;
    }

    /**
     * Sets component attribute to chosen @value. Always uses the first attribute table, unless a more specific attribute
     * locator provided (e.g. @attributename="table2:onChange").
     *
     * @param attributeName name of the attribute (attach prefix of the attribute table if needed another attribute table than
     * the first one)
     * @param value value, which String representation will be set to attribute input.
     */
    protected void setAttribute(String attributeName, Object value) {
        getUnsafeAttributes("").set(attributeName, value);
    }

    /**
     * Waiting method. Waits number of milis defined by @milis
     *
     * @param milis
     */
    protected static void waiting(long milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException ignored) {
        }
    }

    protected void assertNotPresent(WebElement element, String msg) {
        assertTrue(new WebElementConditionFactory(element).not().isPresent().apply(driver), msg);
    }

    protected void assertNotVisible(WebElement element, String msg) {
        assertTrue(new WebElementConditionFactory(element).not().isVisible().apply(driver), msg);
    }

    protected void assertVisible(AdvancedVisibleComponentIteractions<?> o) {
        assertTrue(o.advanced().isVisible());
    }

    protected void assertVisible(AdvancedVisibleComponentIteractions<?> o, String msg) {
        assertTrue(o.advanced().isVisible(), msg);
    }

    protected void assertNotVisible(AdvancedVisibleComponentIteractions<?> o) {
        assertFalse(o.advanced().isVisible());
    }

    protected void assertNotVisible(AdvancedVisibleComponentIteractions<?> o, String msg) {
        assertFalse(o.advanced().isVisible(), msg);
    }

    protected void assertNotVisible(VisibleComponent component, String msg) {
        assertFalse(component.isVisible(), msg);
    }

    protected void assertPresent(WebElement element, String msg) {
        assertTrue(new WebElementConditionFactory(element).isPresent().apply(driver), msg);
    }

    protected void assertVisible(VisibleComponent component, String msg) {
        assertTrue(component.isVisible(), msg);
    }

    protected void assertVisible(WebElement element, String msg) {
        assertTrue(new WebElementConditionFactory(element).isVisible().apply(driver), msg);
    }

    /**
     * Executes JavaScript script.
     *
     * @param script whole command that will be executed
     * @param args
     * @return may return a value or null (if expected (non-returning script) or if returning script fails)
     */
    protected Object executeJS(String script, Object... args) {
        return executor.executeScript(script, args);
    }

    /**
     * Tries to execute JavaScript script for few times and expects a
     *
     * @expectedValue as result. Returns single trimmed String with expected value or what it found or null.
     *
     * @param expectedValue expected return value of javaScript
     * @param script whole JavaScript that will be executed
     * @param args
     * @return single and trimmed string or null
     */
    protected String expectedReturnJS(String script, String expectedValue, Object... args) {
        String result = null;
        for (int i = 0; i < TRIES; i++) {
            try {
                Object executedScriptResult = executeJS(script, args);
                if (executedScriptResult != null) {
                    result = (String.valueOf(executedScriptResult)).trim();
                    if (result.equals(expectedValue)) {
                        return result;
                    }
                }
            } catch (WebDriverException ignored) {
                // e.g. when retrieved property is not defined
            }
            waiting(100);
        }
        return result;
    }

    /**
     * Helper method for testing data.
     *
     * @param triggeringAction
     */
    protected void testData(Action triggeringAction) {
        String testedValue = "RF5";
        attsSetter()
            .setAttribute("data").toValue(testedValue)
            .setAttribute("oncomplete").toValue("data = event.data")
            .asSingleAction().perform();
        Graphene.guardAjax(new ActionWrapper(triggeringAction)).perform();
        assertEquals(expectedReturnJS("return window.data;", testedValue), testedValue);
    }

    /**
     * Test attribute @limitRender. Sets @render attribute to '@this renderChecker' and @limitRender to 'true' and performs an
     * ajax-guarded action.
     */
    protected void testLimitRender(Action triggeringAction) {
        attsSetter()
            .setAttribute("limitRender").toValue(true)
            .setAttribute("render").toValue("@this renderChecker")
            .asSingleAction().perform();
        String renderCheckerText = metamerPage.getRenderCheckerOutputElement().getText();
        String requestTime = metamerPage.getRequestTimeElement().getText();
        Graphene.guardAjax(new ActionWrapper(triggeringAction)).perform();
        Graphene.waitGui().until().element(metamerPage.getRenderCheckerOutputElement()).text().not()
            .equalTo(renderCheckerText);
        Graphene.waitGui().until().element(metamerPage.getRequestTimeElement()).text()
            .equalTo(requestTime);
    }

    /**
     * Same as testLimitRender, but tries to set @switchType or @mode to 'ajax'
     */
    protected void testLimitRenderWithSwitchTypeOrMode(Action triggeringAction) {
        setModeOrSwitchTypeToAjax();
        testLimitRender(triggeringAction);
    }

    /**
     * Test attribute @render. Sets mentioned attribute to '@this renderChecker' and performs an ajax-guarded action. * .
     */
    protected void testRender(Action triggeringAction) {
        getUnsafeAttributes().set("render", "@this renderChecker");
        String renderCheckerText = metamerPage.getRenderCheckerOutputElement().getText();
        String requestTime = metamerPage.getRequestTimeElement().getText();
        Graphene.guardAjax(new ActionWrapper(triggeringAction)).perform();
        Graphene.waitGui().until().element(metamerPage.getRenderCheckerOutputElement()).text().not()
            .equalTo(renderCheckerText);
        Graphene.waitGui().until().element(metamerPage.getRequestTimeElement()).text().not()
            .equalTo(requestTime);
    }

    /**
     * Same as testRender, but tries to set @switchType or @mode to 'ajax'
     */
    protected void testRenderWithSwitchTypeOrMode(Action triggeringAction) {
        setModeOrSwitchTypeToAjax();
        testRender(triggeringAction);
    }

    private void setModeOrSwitchTypeToAjax() {
        UnsafeAttributes attributes = getUnsafeAttributes("");
        try {
            attributes.set("mode", SwitchType.ajax);
            return;
        } catch (RuntimeException ignored) {
        }
        try {
            attributes.set("switchType", SwitchType.ajax);
            return;
        } catch (RuntimeException ignored) {
        }
        fail("Neither of @mode nor @switchType was found.");
    }

    /**
     * Testing of HTMLAttribute (e.g. type).
     *
     * E.g. testHTMLAttribute(page.link, mediaOutputAttributes, MediaOutputAttributes.type, "text/html");
     *
     * @param element WebElement which will be checked for containment of tested attribute
     * @param attributes attributes instance which will be used for setting attribute
     * @param testedAttribute attribute which will be tested
     * @param value tested value of attribute
     */
    protected <T extends AttributeEnum> void testHTMLAttribute(WebElement element, Attributes<T> attributes, T testedAttribute,
        String value) {
        attributes.set(testedAttribute, value);
        String attString = Attribute2StringDecoder.decodeAttribute(testedAttribute);
        String valueOnPage = element.getAttribute(attString);
        if (new StringEqualsWrapper(value).equalsToSomeOfThis(null, "", "null")) {
            if (new StringEqualsWrapper(valueOnPage).notEqualsToSomeOfThis(null, "", "null")) {
                fail("Attribute " + testedAttribute.toString() + " does not work properly, Value of attribute on page: '"
                    + valueOnPage + "', expected value '" + value + "'.");
            }
        } else if (!valueOnPage.contains(value)) {// Attribute has not been set correctly
            fail("Attribute " + testedAttribute.toString() + " does not work properly, Value of attribute on page: '"
                + valueOnPage + "', expected value '" + value + "'.");
        }
    }

    /**
     * Testing of HTMLAttribute (e.g. type).
     *
     * E.g. testHTMLAttribute(page.link, mediaOutputAttributes, MediaOutputAttributes.type, "text/html");
     *
     * @param element FutureTarget of WebElement which will be checked for containment of tested attribute
     * @param attributes attributes instance which will be used for setting attribute
     * @param testedAttribute attribute which will be tested
     * @param value tested value of attribute
     * @param actionAfterSettingOfAttribute action which will be performed after setting the attribute(e.g. open popup), if it
     * is null then it is skipped
     */
    protected <T extends AttributeEnum> void testHTMLAttribute(FutureTarget<WebElement> element, Attributes<T> attributes,
        T testedAttribute, String value, Action actionAfterSettingOfAttribute) {
        attributes.set(testedAttribute, value);
        if (actionAfterSettingOfAttribute != null) {
            actionAfterSettingOfAttribute.perform();
        }
        String attString = Attribute2StringDecoder.decodeAttribute(testedAttribute);
        String valueOnPage = element.getTarget().getAttribute(attString);
        if (new StringEqualsWrapper(value).equalsToSomeOfThis(null, "", "null")) {
            if (new StringEqualsWrapper(valueOnPage).notEqualsToSomeOfThis(null, "", "null")) {
                fail("Attribute " + testedAttribute.toString() + " does not work properly, Value of attribute on page: '"
                    + valueOnPage + "', expected value '" + value + "'.");
            }
        } else if (!valueOnPage.contains(value)) {// Attribute has not been set correctly
            fail("Attribute " + testedAttribute.toString() + " does not work properly, Value of attribute on page: '"
                + valueOnPage + "', expected value '" + value + "'.");
        }
    }

    /**
     * Testing of HTMLAttribute (e.g. type). Expects that if an attribute is set to @value, then the value will be set to
     *
     * @anotherValue (e.g. null -> submit for a4j:commandButton)
     *
     * E.g. testHTMLAttribute(page.link, mediaOutputAttributes, MediaOutputAttributes.type, "text/html");
     *
     * @param element WebElement which will be checked for containment of tested attribute
     * @param attributes attributes instance which will be used for setting attribute
     * @param testedAttribute attribute which will be tested
     * @param value tested value of attribute
     * @param anotherValue value that will replace @value
     */
    protected <T extends AttributeEnum> void testHTMLAttribute(WebElement element, Attributes<T> attributes, T testedAttribute,
        String value, String anotherValue) {
        attributes.set(testedAttribute, value);
        String attString = Attribute2StringDecoder.decodeAttribute(testedAttribute);
        String valueOnPage = element.getAttribute(attString);
        if (new StringEqualsWrapper(value).equalsToSomeOfThis(null, "", "null")) {
            if (new StringEqualsWrapper(anotherValue).isNotSimilarToSomeOfThis(valueOnPage)) {
                fail("Attribute " + testedAttribute.toString() + " does not work properly, Value of attribute on page: '"
                    + valueOnPage + "', expected value '" + anotherValue + "'.");
            }
        } else if (new StringEqualsWrapper(anotherValue).isNotSimilarToSomeOfThis(value)) {// Attribute has not been set
            // correctly
            fail("Attribute " + testedAttribute.toString() + " does not work properly, Value of attribute on page: '"
                + valueOnPage + "', expected value '" + anotherValue + "'.");
        }
    }

    /**
     * Testing of HTMLAttribute. The tested value is RichFaces 4.
     *
     * @param element WebElement which will be checked for containment of tested attribute
     * @param attributes attributes instance which will be used for setting attribute
     * @param testedAttribute attribute which will be tested
     */
    protected <T extends AttributeEnum> void testHTMLAttribute(WebElement element, Attributes<T> attributes, T testedAttribute) {
        testHTMLAttribute(element, attributes, testedAttribute, "RichFaces 4");
    }

    /**
     * Tests lang attribute of chosen component in Metamer. Page must contain an input for this component's attribute.
     *
     * @param element WebElement representing component.
     */
    protected void testLang(WebElement element) {
        final String TESTVALUE = "cz";
        String attLang;
        // set lang to TESTVALUE
        getBasicAttributes().set(BasicAttributes.lang, TESTVALUE);
        // get attribute lang of element
        String lang1 = element.getAttribute("xml:lang");
        String lang2 = element.getAttribute("lang");

        attLang = (lang1 == null || lang1.isEmpty() ? lang2 : lang1);
        assertEquals(attLang, TESTVALUE, "Attribute xml:lang should be present.");
    }

    /**
     * Helper method for testing of delays (showDelay, hideDelay). Runs the @actionWithDelay 4 times and measure time spent in
     * it. Then count a median from these 4 values and asserts it to the @expectedDelay with 50% tolerance.
     *
     * @param actionBefore action before the measured action. Can be used for e.g. close/open menu. Can be null.
     * @param actionWithDelay the measured action. Can be e.g. open/close menu.
     * @param attributeName name of the measured attribute (e.g. hideDelay, showDelay).
     * @param expectedDelayInMillis expected delay spent in @actionWithDelay and also a value that will be set in attribute with
     * name @attributeName
     */
    protected void testDelay(final Action actionBefore, final Action actionWithDelay, String attributeName, long expectedDelayInMillis) {
        getUnsafeAttributes("").set(attributeName, expectedDelayInMillis);
        double tolerance = expectedDelayInMillis == 0 ? 500 : expectedDelayInMillis * 0.5;
        int cycles = 4;
        List<Long> delays = Lists.newArrayList();
        for (int i = 0; i < cycles; i++) {
            //This is debug output, to determine in which cycle test falls.
            System.out.println(format("Tested delay: {0}, cycle: {1}", expectedDelayInMillis, i));
            if (actionBefore != null) {
                actionBefore.perform();
            }
            delays.add(StopWatch.watchTimeSpentInAction(actionWithDelay).inMillis().longValue());
        }
        Number median = countMedian(delays);
        assertEquals(median.doubleValue(), expectedDelayInMillis, tolerance, "The delay is not in tolerance. Median of delays was " + median);
    }

    /**
     * A helper method for testing attribute "dir". It tries null, ltr and rtl.
     *
     * @param element WebElement reference of tested element
     */
    protected void testDir(WebElement element) {
        testHTMLAttribute(element, getBasicAttributes(), BasicAttributes.dir, "null");
        testHTMLAttribute(element, getBasicAttributes(), BasicAttributes.dir, "ltr");
        testHTMLAttribute(element, getBasicAttributes(), BasicAttributes.dir, "rtl");
    }

    /**
     * Use with <code>@UseWithField(field = "positioning",valuesFrom = FROM_ENUM, value = "")</code>.
     *
     * @param showAction action which will show the tested menu and will return it as a WebElement.
     */
    protected void testDirection(ShowElementAndReturnAction showAction) {
        testPositioning(showAction, 0, 0);
    }

    /**
     * Use with <code>@UseWithField(field = "positioning",valuesFrom = FROM_ENUM, value = "")</code>.
     *
     * @param maxOffSetX width of the menu item, input, etc. from which will be the menu displayed
     * @param maxOffsetY height of the menu item, input, etc. from which will be the menu displayed
     * @param showAction action which will show the tested menu and will return it as a WebElement.
     */
    protected void testJointPoint(int maxOffSetX, int maxOffsetY, ShowElementAndReturnAction showAction) {
        testPositioning(showAction, maxOffSetX, maxOffsetY);
    }

    private void testPositioning(ShowElementAndReturnAction showAction, int maxOffSetX, int maxOffsetY) {
        int tolerance = 10;
        boolean isDirectionTest = (maxOffsetY == 0 && maxOffSetX == 0);
        UnsafeAttributes atts = getUnsafeAttributes("");

        // determine tested attribute
        String testedAtt = isDirectionTest ? "direction" : "jointPoint";

        // set reference values
        if (atts.hasAttribute("jointPoint")) {
            attsSetter()
                .setAttribute("direction").toValue(Positioning.bottomRight)
                .setAttribute("jointPoint").toValue(Positioning.bottomRight)
                .asSingleAction().perform();
        } else {// no @jointPoint attribute, set direction only
            atts.set("direction", Positioning.bottomRight);
        }
        // get reference locations
        Locations locationsBottomRight = Utils.getLocations(showAction.perform());

        // setup tested attribute
        atts.set(testedAtt, positioning);
        if (atts.get(testedAtt).equals(Positioning.bottomRight.toString())) {
            // reload the page to dismiss the popup
            openPageWithCurrentConfiguration();
        }
        // get new locations, with tested value of @direction/@jointPoint
        Locations locationsAfterReposition = Utils.getLocations(showAction.perform());

        int widthChange = maxOffSetX;
        int heightChange = maxOffsetY;
        if (isDirectionTest) {
            widthChange = locationsBottomRight.getWidth();
            heightChange = locationsBottomRight.getHeight();
        }

        // check
        if (STRICT_POSITIONING.contains(positioning)) {
            Utils.tolerantAssertLocationsEquals(getLocationsOfBottomRightAfterPositioningChanges(locationsBottomRight, positioning, widthChange, heightChange), locationsAfterReposition, tolerance, tolerance, "");
        } else {// some '*auto*' option
            // cycle through all strict directions, one must be the same as the '*auto*',
            // which  one it will be depends on browser/screen resolution and actual position
            for (Positioning pos : STRICT_POSITIONING) {
                try {
                    Utils.tolerantAssertLocationsEquals(getLocationsOfBottomRightAfterPositioningChanges(locationsBottomRight, pos, widthChange, heightChange), locationsAfterReposition, tolerance, tolerance, "");
                    return;
                } catch (AssertionError ignored) {
                }
            }
            fail("No position was close enough for direction " + positioning.toString());
        }
    }

    private Locations getLocationsOfBottomRightAfterPositioningChanges(Locations locations, Positioning jointPoint, int width, int height) {
        switch (jointPoint) {
            case topLeft:
                return locations.moveAllBy(-width, -height);
            case topRight:
                return locations.moveAllBy(0, -height);
            case bottomLeft:
                return locations.moveAllBy(-width, 0);
            case bottomRight:
                return locations;
            default:
                throw new UnsupportedOperationException("Not supported positioning: " + jointPoint);
        }
    }

    /**
     * @param showAction action which will show the tested menu and will return it as a WebElement.
     */
    protected void testHorizontalOffset(ShowElementAndReturnAction showAction) {
        testOffset(false, showAction);
    }

    /**
     * @param showAction action which will show the tested menu and will return it as a WebElement.
     */
    protected void testVerticalOffset(ShowElementAndReturnAction showAction) {
        testOffset(true, showAction);
    }

    private void testOffset(boolean isVerticalOffset, ShowElementAndReturnAction showAction) {
        int tolerance = 10;
        int testedOffset = 50;

        try {
            getUnsafeAttributes("").set("direction", Positioning.bottomRight);
        } catch (AttributeNotSetException ignored) {
        }
        try {
            getUnsafeAttributes("").set("jointPoint", Positioning.bottomRight);
        } catch (AttributeNotSetException ignored) {
        }

        Locations locationsBefore = Utils.getLocations(showAction.perform());

        getUnsafeAttributes("").set((isVerticalOffset ? "verticalOffset" : "horizontalOffset"), testedOffset);
        Locations locationsAfter = Utils.getLocations(showAction.perform());

        Utils.tolerantAssertLocationsEquals(locationsAfter, locationsBefore.moveAllBy((isVerticalOffset ? 0 : testedOffset), (isVerticalOffset ? testedOffset : 0)), tolerance, tolerance, "");
    }

    /**
     * A helper method for testing JavaScripts events. It sets "metamerEvents += "testedAttribute" to the input field of the
     * tested attribute and fires the event @event using jQuery on the given element @element. Then it checks if the event was
     * fired. This method should only be used for attributes consistent with DOM events (e.g. (on)click, (on)change...).
     *
     * @param element WebElement on which will be the event triggered
     * @param attributes attributes instance which will be used for setting attribute
     * @param testedAttribute attribute which will be tested
     */
    protected <T extends AttributeEnum> void testFireEventWithJS(WebElement element, Attributes<T> attributes, final T testedAttribute) {
        attributes.set(testedAttribute, "metamerEvents += \"" + testedAttribute.toString() + " \"");
        executeJS("metamerEvents = \"\";");
        Event e = new Event(testedAttribute.toString().substring(2));// remove prefix "on"
        fireEvent(element, e);

        Graphene.waitGui().withMessage("Event " + e + " does not work.").until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver wd) {
                String metamerEvents = executor.executeScript("return metamerEvents").toString().trim();
                return testedAttribute.toString().equals(metamerEvents);
            }
        });
    }

    /**
     * A helper method for testing JavaScripts events. It sets "metamerEvents += "testedAttribute" to the input field of the
     * tested attribute and fires the event @event using jQuery on the element @element. Then it checks if the event was fired.
     *
     * @event using jQuery on the element
     * @element. Then it checks if the event was fired.
     *
     * @see testFireEventWithJS(WebElement element, Attributes<T> attributes, T testedAttribute)
     * @param element WebElement on which will be the event triggered
     * @param event event wich will be triggered
     * @param attributes attributes instance which will be used for setting attribute
     * @param testedAttribute attribute which will be tested
     */
    protected <T extends AttributeEnum> void testFireEventWithJS(WebElement element, Event event, Attributes<T> attributes,
        T testedAttribute) {
        attributes.set(testedAttribute, "metamerEvents += \"" + testedAttribute.toString() + " \"");
        executeJS("metamerEvents = \"\";");
        fireEvent(element, event);
        String returnedString = expectedReturnJS("return metamerEvents", testedAttribute.toString());
        assertEquals(returnedString, testedAttribute.toString(), "Event " + event + " does not work.");
    }

    /**
     * A helper method for testing events. It sets "metamerEvents += "@testedAttribute" to the input field and fires the event
     * using Actions. Then it checks if the event was fired.
     *
     * @param attributes attributes instance which will be used for setting attribute
     * @param testedAttribute attribute which will be tested
     * @param eventFiringAction selenium action which leads to launch the tested event,
     */
    protected <T extends AttributeEnum> void testFireEvent(Attributes<T> attributes, T testedAttribute, Action eventFiringAction) {
        attributes.set(testedAttribute, "metamerEvents += \"" + testedAttribute.toString() + " \"");
        executeJS("metamerEvents = \"\";");
        try {
            eventFiringAction.perform();
            String returnedString = expectedReturnJS("return metamerEvents", testedAttribute.toString());
            assertEquals(returnedString, testedAttribute.toString(), "Event " + testedAttribute.toString() + " does not work.");
        } finally {
            if (testedAttribute.toString().toLowerCase().endsWith("mousedown")) {
                tryToReleaseMouseButton();
            }
        }

    }

    /**
     * A helper method for testing JavaScripts events.
     *
     * @param event JavaScript event to be tested
     * @param element element on which will be the event triggered
     */
    protected void testFireEvent(Event event, WebElement element) {
        testFireEvent(event, element, event.getEventName());
    }

    /**
     * A helper method for testing JavaScripts events.
     *
     * @param event JavaScript event to be tested
     * @param element FutureTarget of WebElement on which will be the event triggered
     * @param actionBeforeFiringTheEvent action which will be performed before firing the event
     */
    protected void testFireEvent(final Event event, final FutureTarget<WebElement> element,
        final Action actionBeforeFiringTheEvent) {
        testFireEvent(event.getEventName(), new Action() {
            @Override
            public void perform() {
                if (actionBeforeFiringTheEvent != null) {
                    actionBeforeFiringTheEvent.perform();
                }
                fireEvent(element.getTarget(), event);
            }
        });
    }

    /**
     * A helper method for testing JavaScripts events.
     *
     * @param event JavaScript event to be tested
     * @param element element on which will be the event triggered (must be present before test, or a proxy)
     * @param attributeName name of the attribute that should be set
     */
    protected void testFireEvent(final Event event, final WebElement element, String attributeName) {
        testFireEvent(attributeName, new Action() {
            @Override
            public void perform() {
                fireEvent(element, event);
            }
        });
    }

    /**
     * A helper method for testing events.
     *
     * @param attributeName name of the attribute that should be set (i.e. inputselect, onselect ; can be without the prefix
     * 'on')
     * @param eventFiringAction action which will be performed to trigger the event
     */
    protected void testFireEvent(String attributeName, Action eventFiringAction) {
        setAttribute((attributeName.startsWith("on") ? attributeName : "on" + attributeName), "metamerEvents += \""
            + attributeName + " \"");
        // clear/init events
        executeJS("metamerEvents = \"\";");
        // trigger event
        try {
            eventFiringAction.perform();
            // check
            assertEquals(expectedReturnJS("return metamerEvents", attributeName), attributeName, "Attribute " + attributeName
                + " does not work.");
        } finally {
            if (attributeName.toLowerCase().endsWith("mousedown")) {
                tryToReleaseMouseButton();
            }
        }
    }

    private void tryToReleaseMouseButton() {
        try {
            new Actions(driver).release().perform();
        } catch (WebDriverException ignored) {
        }
    }

    /**
     * Method for firing JavaScript events on given element using jQuery.
     *
     * @param element
     * @param event
     */
    protected void fireEvent(WebElement element, Event event) {
        Utils.triggerJQ(executor, event.getEventName(), element);
    }

    /**
     * Helper method for testing label's text changing. At first it sets "RichFaces 4" to the <code>testedAttribute</code>
     * input, then fires <code>labelChangeAction</code>(if some), then waits for the visibility of <code>element</code> and
     * finally checks if the label (<code>getText()</code> method) of <code>element</code> was changed as expected.
     *
     * @param element element which <code>getText()</code> method will be used for checking of label text
     * @param attributes attributes instance which will be used for setting attribute
     * @param testedAttribute attribute which will be tested
     * @param labelChangeAction action which will change the label (if no action needed use <code>null</code> or empty Action)
     */
    protected <T extends AttributeEnum> void testLabelChanges(WebElement element, Attributes<T> attributes, T testedAttribute,
        Action labelChangeAction) {
        testLabelChanges(FutureWebElement.of(element), attributes, testedAttribute, labelChangeAction);
    }

    protected <T extends AttributeEnum> void testLabelChanges(FutureTarget<WebElement> futureTarget, Attributes<T> attributes,
        T testedAttribute, Action labelChangeAction) {
        String rf = "RichFaces 4";
        attributes.set(testedAttribute, rf);
        if (labelChangeAction != null) {
            labelChangeAction.perform();
        }
        Graphene.waitModel().until().element(futureTarget.getTarget()).is().visible();
        Graphene.waitModel().until(testedAttribute + " does not work, label has not changed.")
            .element(futureTarget.getTarget()).text().equalTo(rf);
    }

    protected <T extends AttributeEnum> void testLabelChanges(String attributeName, FutureTarget<WebElement> futureTarget,
        Action labelChangeAction) {
        String rf = "RichFaces 4";
        setAttribute(attributeName, rf);
        if (labelChangeAction != null) {
            labelChangeAction.perform();
        }
        Graphene.waitModel().until().element(futureTarget.getTarget()).is().visible();
        Graphene.waitModel().until(attributeName + " does not work, label has not changed.").element(futureTarget.getTarget())
            .text().equalTo(rf);
    }

    /**
     * Helper method for testing of attribute 'status'. At first it sets @status to "statusChecker", then saves Metamer's
     * 'statusCheckerOutput' time, then fires <code>statusChangingAction</code> (if not null), and finally checks if Metamer's
     * 'statusCheckerOutput' time was changed before Graphene.waitModel() interval expires.
     *
     * @param statusChangingAction action that will change the status. Can be null.
     */
    protected void testStatus(Action statusChangingAction) {
        String checker = "statusChecker";
        // set attribute
        getUnsafeAttributes("").set("status", checker);

        String statusCheckerTimeBefore = metamerPage.getStatusCheckerOutputElement().getText();
        if (statusChangingAction != null) {
            Graphene.guardAjax(new ActionWrapper(statusChangingAction)).perform();
        }
        Graphene.waitModel().until().element(metamerPage.getStatusCheckerOutputElement()).text().not()
            .equalTo(statusCheckerTimeBefore);
    }

    /**
     * A helper method for testing attribute "style" or similar. It sets "background-color: yellow; font-size: 1.5em;" to the
     * input field and checks that it was changed on the page.
     *
     * @param element WebElement reference of tested element
     * @param attribute name of the attribute that will be set (e.g. style, headerStyle, itemContentStyle)
     */
    protected void testStyle(final WebElement element, BasicAttributes attribute) {
        final String value = "background-color: yellow;";
        testHTMLAttribute(element, getBasicAttributes(), attribute, value);
    }

    /**
     * A helper method for testing attribute "style". It sets "background-color: yellow; font-size: 1.5em;" to the input field
     * and checks that it was changed on the page.
     *
     * @param element WebElement reference of tested element
     */
    protected void testStyle(final WebElement element) {
        testStyle(element, BasicAttributes.style);
    }

    /**
     * A helper method for testing attribute "class" or similar. It sets "metamer-ftest-class" to the input field and checks
     * that it was changed on the page.
     *
     * @param element WebElement reference of tested element
     * @param attribute name of the attribute that will be set (e.g. styleClass, headerClass, itemContentClass)
     */
    protected void testStyleClass(WebElement element, BasicAttributes attribute) {
        final String styleClass = "metamer-ftest-class";
        testHTMLAttribute(element, getBasicAttributes(), attribute, styleClass);
    }

    /**
     * A helper method for testing attribute "class". It sets "metamer-ftest-class" to the input field and checks that it was
     * changed on the page.
     *
     * @param element locator of tested element
     */
    protected void testStyleClass(WebElement element) {
        testStyleClass(element, BasicAttributes.styleClass);
    }

    /**
     * A helper method for testing attribute "title".
     *
     * @param element WebElement reference of tested element
     */
    protected void testTitle(WebElement element) {
        final String testTitle = "RichFaces 4";
        testHTMLAttribute(element, getBasicAttributes(), BasicAttributes.title, testTitle);
    }

    /**
     * Helper method for testing table's style class attributes (e.g. cellClass, columnFooterClass)
     *
     * @param attributeName
     * @param elementClassName
     * @param expectedCount
     */
    protected void testTableStyleClass(String attributeName, String elementClassName, int expectedCount) {
        String klass = "metamer-ftest-class";
        setAttribute(attributeName, klass);
        List<WebElement> elems = driver.findElements(By.className(elementClassName));
        assertTrue(expectedCount > 0);
        assertEquals(elems.size(), expectedCount);
        for (WebElement elem : elems) {
            assertTrue(elem.getAttribute("class").contains(klass));
        }
    }

    /**
     * Checks that there is no 404 error in browser's console.
     *
     * @param beforeCheckAction action berformed before checking. Can be null (no action will be performed).
     */
    protected void checkNoResourceErrorPresent(Action beforeCheckAction) {
        // perform action
        if (beforeCheckAction != null) {
            beforeCheckAction.perform();
        }
        final String errMsg = "failed to load resource";
        for (String msg : jsErrorStorage.getMessages()) {
            assertFalse(msg.contains(errMsg), "There should be no resource error message in browser's console. Had: <" + msg + ">.");
        }
    }

    /**
     * Tries to check and wait for correct size (@size) of list. Depends on list of WebElements decorated with
     * StaleReferenceAwareFieldDecorator.
     *
     * @param list input list
     * @param size expected size of list
     * @return list with or without expected size
     */
    protected List<WebElement> guardListSize(List<WebElement> list, int size) {
        boolean lastCheckWithModifications;
        int checkedSize = list.size();
        for (int i = 0; i < TRIES; i++) {
            if (checkedSize < list.size()) {
                checkedSize = list.size();
                lastCheckWithModifications = true;
            } else {
                lastCheckWithModifications = false;
            }
            if (checkedSize >= size && !lastCheckWithModifications) {
                // last check
                waiting(MINOR_WAIT_TIME);
                list.size();
                return list;
            }
            waiting(MINOR_WAIT_TIME);
        }
        return list;
    }

    protected <T extends AttributeEnum> Attributes<T> getAttributes(String attributesTableId) {
        return AttributesImpl.<T>getAttributesFor(testResourcesProvider, attributesTableId);
    }

    protected <T extends AttributeEnum> Attributes<T> getAttributes() {
        return getAttributes("");
    }

    protected UnsafeAttributes getUnsafeAttributes(String attributesTableId) {
        return new AttributesImpl<AttributeEnum>(testResourcesProvider, attributesTableId);
    }

    protected UnsafeAttributes getUnsafeAttributes() {
        return getUnsafeAttributes("");
    }

    /**
     * Method used to run selenium test in portal environment.
     */
    private void goToTestInPortal() {
        driver.get(String.format("%s://%s:%s/%s", contextPath.getProtocol(), contextPath.getHost(), contextPath.getPort(),
            "portal/classic/metamer"));
        try {
            driver.findElement(By.cssSelector("a[id$='controlsForm:goHomeLink']")).click();
            // JSF form works only on home page
        } catch (NoSuchElementException ex) {
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String setTextQuery = "document.querySelector(\"input[id$='linksForm:%s']\").value = '%s';";
        String testUrl = getTestUrl().toExternalForm().substring(getTestUrl().toExternalForm().indexOf("faces"));
        js.executeScript(String.format(setTextQuery, "linkToTest", testUrl));
        js.executeScript(String.format(setTextQuery, "template", template.toString()));
        js.executeScript("document.querySelector(\"a[id$='linksForm:redirectToPortlet']\").click()");
    }

    public void testRequestEventsBefore(String... events) {
        String testedEventTrueName;
        MultipleAttributesSetter attsSetter = attsSetter();
        for (String event : events) {
            testedEventTrueName = event;
            if (!testedEventTrueName.startsWith("on")) {
                testedEventTrueName = format("on{0}", testedEventTrueName);
            }
            attsSetter
                .setAttribute(testedEventTrueName)
                .toValue(format("sessionStorage.setItem(\"metamerEvents\", sessionStorage.getItem(\"metamerEvents\") + \"{0} \")", event));
        }
        attsSetter.asSingleAction().perform();
        cleanMetamerEventsVariable();
    }

    public void testRequestEventsAfter(final String... events) {
        Graphene.waitModel().until(new Predicate<WebDriver>() {
            private String actualEvents;
            private int lastNumberOfEvents;

            @Override
            public boolean apply(WebDriver arg0) {
                actualEvents = ((String) executeJS("return sessionStorage.getItem(\"metamerEvents\")"));
                lastNumberOfEvents = (StringUtils.isBlank(actualEvents) ? 0 : actualEvents.split(" ").length);
                return lastNumberOfEvents == events.length;
            }

            @Override
            public String toString() {
                return format("number of events is equal to {0}, found {1}. Actual events: {2}", events.length,
                    lastNumberOfEvents, actualEvents);
            }

        });
        String[] actualEvents = ((String) executeJS("return sessionStorage.getItem(\"metamerEvents\")")).split(" ");
        assertEquals(
            actualEvents,
            events,
            String.format("The events (%s) don't came in right order (%s)", Arrays.deepToString(actualEvents),
                Arrays.deepToString(events)));
    }

    public MultipleEventFirerer getMultipleEventsFirerer() {
        return new MultipleEventFirererImpl(executor);
    }

    public void cleanMetamerEventsVariable() {
        // since metamerEvents variable stored on session too, make sure that cleaned both of them
        executeJS("window.metamerEvents = \"\";");
        executeJS("sessionStorage.setItem('metamerEvents',\"\")");
    }

    public static <T extends Number & Comparable<T>> Number countMedian(List<T> values) {
        assertTrue(values.size() > 0);
        if (values.size() == 1) {
            return values.get(0);
        }

        final List<T> copy = Lists.newArrayList(values);
        Collections.sort(copy);

        int middleIndex = (copy.size() - 1) / 2;

        double result = copy.get(middleIndex).doubleValue();
        if (copy.size() % 2 == 0) {
            result = (result + copy.get(middleIndex + 1).doubleValue()) / 2.0;
        }
        final Double median = Double.valueOf(result);
        return new Number() {
            private static final long serialVersionUID = 1L;

            @Override
            public int intValue() {
                return median.intValue();
            }

            @Override
            public long longValue() {
                return median.longValue();
            }

            @Override
            public float floatValue() {
                return median.floatValue();

            }

            @Override
            public double doubleValue() {
                return median.doubleValue();

            }

            @Override
            public String toString() {
                return median.doubleValue() + " from values(sorted) " + copy.toString() + '.';
            }
        };
    }

    /**
     * Decoder for Attributes. Converts given Attribute to String. If Attribute ends with 'class' or 'style', then it returns
     * the correct one, when the attribute does not end with none of those, then it returns toString() method of attribute
     */
    public static class Attribute2StringDecoder {

        private static final String[] ATTRIBUTES = { "class", "classes", "style" };

        public static <T extends AttributeEnum> String decodeAttribute(T testedAttribute) {
            String testedAtt = testedAttribute.toString();
            if (testedAtt.length() > 6) {
                // get the ending
                String tmp = testedAtt.toLowerCase();
                for (String string : ATTRIBUTES) {
                    if (tmp.lastIndexOf(string) > 0) {// contains an attribute to decode
                        if (string.equalsIgnoreCase(ATTRIBUTES[0]) || string.equalsIgnoreCase(ATTRIBUTES[1])) {
                            return ATTRIBUTES[0];
                        } else if (string.equalsIgnoreCase(ATTRIBUTES[2])) {
                            return ATTRIBUTES[2];
                        } else {
                            throw new RuntimeException("Cannot decode attribute " + testedAtt);
                        }
                    }
                }
            }
            return testedAtt;
        }
    }

    /**
     * Abstract ReloadTester for testing component's state after reloading the page
     *
     * @param <T> the type of input values which will be set, sent and then verified
     */
    public abstract class ReloadTester<T> {

        public abstract void doRequest(T inputValue);

        public abstract void verifyResponse(T inputValue);

        public abstract T[] getInputValues();

        public void testRerenderAll() {
            for (T inputValue : getInputValues()) {
                doRequest(inputValue);
                verifyResponse(inputValue);
                getMetamerPage().rerenderAll();
                verifyResponse(inputValue);
            }
        }

        public void testFullPageRefresh() {
            for (T inputValue : getInputValues()) {
                doRequest(inputValue);
                verifyResponse(inputValue);
                getMetamerPage().fullPageRefresh();
                verifyResponse(inputValue);
            }
        }
    }

    protected interface ShowElementAndReturnAction {

        WebElement perform();
    }

    public interface FutureTarget<T> {

        T getTarget();
    }

    /**
     * Wrapper for anonymous actions, so it can be guarded by graphene.
     */
    public static class ActionWrapper implements Action {

        private final Action a;

        public ActionWrapper(Action a) {
            this.a = a;
        }

        @Override
        public void perform() {
            a.perform();
        }
    }

    public static final class FutureWebElement {

        private FutureWebElement() {
        }

        public static FutureTarget<WebElement> of(final WebElement element) {
            return new FutureTarget<WebElement>() {
                @Override
                public WebElement getTarget() {
                    return element;
                }
            };
        }

        public static FutureTarget<WebElement> of(final By by, final SearchContext context) {

            return new FutureTarget<WebElement>() {
                @Override
                public WebElement getTarget() {
                    return context.findElement(by);
                }
            };
        }
    }

    /**
     * Helper class for testing of popup menu's hide/show delays. Executes the measuring in browser using JavaScript, JS API of
     * the component and attributes 'onhide' and 'onshow' of the component.
     */
    protected class MenuDelayTester {

        private static final String ATTRIBUTE_TIME_NAME = "delayTime";
        private static final String ATTRIBUTE_TRIGGERED_NAME = "delayAttributeTriggered";
        private static final String HIDE_DELAY = "hideDelay";
        private static final String ON_HIDE = "onhide";
        private static final String ON_SHOW = "onshow";
        private static final String SHOW_DELAY = "showDelay";
        private final String GET_TIME_SCRIPT = format("return window.{0};", ATTRIBUTE_TIME_NAME);
        private final String MEASURING_SCRIPT_TEMPLATE = String.format(
            "window.%s = false;"
            + "var event = jQuery.Event(\"click\");"// event type does not matter, but has to be non-empty
            + "event.pageX = {0};"
            + "event.pageY = {1};"
            + "RichFaces.component(\"{2}\").{3}(event);"
            + "window.%s = new Date().getTime();",
            ATTRIBUTE_TRIGGERED_NAME,
            ATTRIBUTE_TIME_NAME);
        private final String PREPARATION_SCRIPT = format("window.{0}=new Date().getTime() - window.{0}; window.{1}=true;",
            ATTRIBUTE_TIME_NAME, ATTRIBUTE_TRIGGERED_NAME);

        public MenuDelayTester() {
        }

        private String getMeasuringScript(WebElement rootElement, boolean isHideDelay, Event[] triggerEvents) {
            Point location = rootElement.getLocation();
            Locations locations = Utils.getLocations(rootElement);
            int x = location.x + locations.getWidth() / 2;
            int y = location.y + locations.getHeight() / 2;
            String id = rootElement.getAttribute("id");
            String first = isHideDelay ? "show" : "hide";
            StringBuilder sb = new StringBuilder(MEASURING_SCRIPT_TEMPLATE);
            for (Event e : triggerEvents) {
                sb.append("event = jQuery.Event(\"").append(e)
                    .append("\");event.pageX={0};event.pageY = {1};jQuery(arguments[0]).trigger(event);");
            }
            return format(sb.toString(), x, y, id, first);
        }

        private void testDelay(boolean isHideDelay, final WebElement menuRootElement, final long expectedDelayInMillis, Event[] triggerEvent, WebElement triggerEventOnElement) {
            double tolerance = expectedDelayInMillis == 0 ? 500 : expectedDelayInMillis * 0.5;
            int cycles = 4;
            attsSetter()
                .setAttribute(isHideDelay ? HIDE_DELAY : SHOW_DELAY).toValue(expectedDelayInMillis)// set tested attribute
                .setAttribute(isHideDelay ? SHOW_DELAY : HIDE_DELAY).toValue(0)// reset not tested attribute
                .setAttribute(isHideDelay ? ON_HIDE : ON_SHOW).toValue(PREPARATION_SCRIPT)
                .asSingleAction().perform();
            List<Long> delays = new ArrayList<Long>(cycles);
            String measuringScript = getMeasuringScript(menuRootElement, isHideDelay, triggerEvent);
            for (int i = 1; i <= cycles; i++) {
                //This is debug output, to determine in which cycle test could fall.
                System.out.println(format("Tested delay: {0} [ms], cycle: {1}", expectedDelayInMillis, i));
                executor.executeScript(measuringScript, triggerEventOnElement);
                Graphene.waitGui()
                    .withTimeout(expectedDelayInMillis * 2, TimeUnit.MILLISECONDS)
                    .until(new EventTriggeredPredicate(ATTRIBUTE_TRIGGERED_NAME));
                delays.add(Long.valueOf(executor.executeScript(GET_TIME_SCRIPT).toString()));
                if (!isHideDelay) {
                    // hide menu after testing @showDelay
                    getMetamerPage().getRequestTimeElement().click();
                }
            }
            Number median = countMedian(delays);
            assertEquals(median.doubleValue(), expectedDelayInMillis, tolerance, "The delay is not in tolerance. Median of"
                + " delays was " + median);
        }

        public void testHideDelay(final WebElement menuRootElement, final long expectedDelayInMillis, Event[] triggerEvents, WebElement triggerEventOnElement) {
            testDelay(Boolean.TRUE, menuRootElement, expectedDelayInMillis, triggerEvents, triggerEventOnElement);
        }

        public void testHideDelay(final WebElement menuRootElement, final long expectedDelayInMillis, Event triggerEvent, WebElement triggerEventOnElement) {
            testHideDelay(menuRootElement, expectedDelayInMillis, new Event[] { triggerEvent }, triggerEventOnElement);
        }

        public void testShowDelay(final WebElement menuRootElement, final long expectedDelayInMillis, Event[] triggerEvents, WebElement triggerEventOnElement) {
            testDelay(Boolean.FALSE, menuRootElement, expectedDelayInMillis, triggerEvents, triggerEventOnElement);
        }

        public void testShowDelay(final WebElement menuRootElement, final long expectedDelayInMillis, Event triggerEvent, WebElement triggerEventOnElement) {
            testDelay(Boolean.FALSE, menuRootElement, expectedDelayInMillis, new Event[] { triggerEvent }, triggerEventOnElement);
        }

        private class EventTriggeredPredicate implements Predicate<WebDriver> {

            private final String eventName;
            private String lastReturnedString;
            private final String valueToEqualTo;

            protected EventTriggeredPredicate(String eventName) {
                this(eventName, "true");
            }

            protected EventTriggeredPredicate(String eventName, String valueToEqualTo) {
                this.eventName = eventName;
                this.valueToEqualTo = valueToEqualTo.toLowerCase();
            }

            @Override
            public boolean apply(WebDriver t) {
                lastReturnedString = executor.executeScript(format("return window.{0};", eventName)).toString().toLowerCase();
                return lastReturnedString.equals(valueToEqualTo);
            }

            @Override
            public String toString() {
                return format("<{0}> to be equal to <{1}>, last returned value was <{2}>.", eventName, valueToEqualTo,
                    lastReturnedString);
            }
        }
    }

    private final TestResourcesProvider testResourcesProvider = new TestResourcesProvider() {

        @Override
        public UnsafeAttributes getAttributes(String attributeTableId) {
            return getUnsafeAttributes(attributeTableId);
        }

        @Override
        public JavascriptExecutor getJSExecutor() {
            return executor;
        }

        @Override
        public WebDriver getWebDriver() {
            return driver;
        }
    };

    public MultipleAttributesSetter attsSetter() {
        return new AttributesHandler(testResourcesProvider);
    }

    @JavaScript("window.JSErrorStorage")
    public interface JSErrorStorage {

        List<String> getMessages();
    }
}
