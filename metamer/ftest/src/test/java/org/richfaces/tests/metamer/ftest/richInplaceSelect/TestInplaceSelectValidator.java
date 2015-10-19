/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010-2015, Red Hat, Inc. and individual contributors
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
package org.richfaces.tests.metamer.ftest.richInplaceSelect;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.support.FindBy;
import org.richfaces.fragment.inplaceSelect.RichFacesInplaceSelect;
import org.richfaces.tests.metamer.ftest.abstractions.validator.AbstractInputComponentValidatorTest;
import org.testng.annotations.Test;

/**
 * Test case for page faces/components/richSelect/validator.xhtml.
 *
 * @author <a href="mailto:jstefek@redhat.com">Jiri Stefek</a>
 */
public class TestInplaceSelectValidator extends AbstractInputComponentValidatorTest {

    private static final int NOT_PHOENIX = 10;
    private static final String PHOENIX = "Phoenix";

    @FindBy(css = "[id$=component]")
    private RichFacesInplaceSelect select;

    @Override
    public String getComponentTestPagePath() {
        return "richInplaceSelect/validator.xhtml";
    }

    @Override
    protected void setCorrectValue() {
        Graphene.guardAjax(select).select(PHOENIX);
    }

    @Override
    protected void setIncorrectValue() {
        Graphene.guardAjax(select).select(NOT_PHOENIX);
    }

    @Test
    public void testValidator() {
        super.testValidator();
    }
}
