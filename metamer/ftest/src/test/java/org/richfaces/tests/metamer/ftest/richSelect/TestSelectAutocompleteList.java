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

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.support.FindBy;
import org.richfaces.fragment.select.RichFacesSelect;
import org.richfaces.fragment.select.SelectSuggestions;
import org.richfaces.tests.metamer.ftest.AbstractWebDriverTest;
import org.richfaces.tests.metamer.ftest.extension.attributes.coverage.annotations.CoversAttributes;
import org.richfaces.tests.metamer.ftest.richSelect.TestSelectAutocompleteMethod.Mode;
import org.richfaces.tests.metamer.ftest.webdriver.Attributes;
import org.testng.annotations.Test;

/**
 * Test case for page faces/components/richSelect/autocompleteList.xhtml.
 *
 * @author <a href="mailto:jstefek@redhat.com">Jiri Stefek</a>
 */
public class TestSelectAutocompleteList extends AbstractWebDriverTest {

    private final Attributes<SelectAttributes> attributes = getAttributes();

    @FindBy(css = "span[id$=output]")
    private WebElement output;
    @FindBy(css = "div[id$=select]")
    private RichFacesSelect select;

    private final Action typeAToTheSelectInputAction = new Action() {
        @Override
        public void perform() {
            select.type("a");
        }
    };

    @Override
    public String getComponentTestPagePath() {
        return "richSelect/autocompleteList.xhtml";
    }

    @Test
    @CoversAttributes({ "autocompleteList", "clientFilterFunction", "itemLabel", "itemValue" })
    public void testAutocompleteFunctionalityWithClientFiltering() {
        attsSetter()
            .setAttribute(SelectAttributes.mode).toValue(Mode.client)
            .setAttribute(SelectAttributes.clientFilterFunction).toValue("filterValuesByContains")
            .asSingleAction().perform();
        SelectSuggestions selectState = Graphene.guardNoRequest(select).type("aw");
        List<WebElement> suggestions = select.advanced().getSuggestionsElements();
        assertEquals(suggestions.size(), 2);
        assertEquals(suggestions.get(0).getText(), "Delaware");
        assertEquals(suggestions.get(1).getText(), "Hawaii");
        Graphene.guardAjax(selectState).select(1);// select Hawaii
        Graphene.waitAjax().until().element(output).text().equalTo("Hawaii");
    }

    @Test
    @CoversAttributes("onbeforedomupdate")
    public void testOnbeforedomupdate() {
        attributes.set(SelectAttributes.mode, "ajax");
        testFireEvent("onbeforedomupdate", typeAToTheSelectInputAction);
    }

    @Test
    @CoversAttributes("onbegin")
    public void testOnbegin() {
        attributes.set(SelectAttributes.mode, "ajax");
        testFireEvent("onbegin", typeAToTheSelectInputAction);
    }

    @Test
    @CoversAttributes("oncomplete")
    public void testOncomplete() {
        attributes.set(SelectAttributes.mode, "ajax");
        testFireEvent("oncomplete", typeAToTheSelectInputAction);
    }

    @Test
    @CoversAttributes("status")
    public void testStatus() {
        attributes.set(SelectAttributes.mode, "ajax");
        testStatus(typeAToTheSelectInputAction);
    }
}
