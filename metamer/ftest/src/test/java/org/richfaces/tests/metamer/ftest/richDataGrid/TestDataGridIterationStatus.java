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
package org.richfaces.tests.metamer.ftest.richDataGrid;

import static org.testng.Assert.assertEquals;

import javax.xml.bind.JAXBException;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.richfaces.fragment.dataGrid.RichFacesDataGrid;
import org.richfaces.tests.metamer.ftest.extension.attributes.coverage.annotations.CoversAttributes;
import org.richfaces.tests.metamer.ftest.richDataGrid.fragment.GridRecordInterface;
import org.richfaces.tests.metamer.ftest.richDataGrid.fragment.GridWithStates;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:jpapouse@redhat.com">Jan Papousek</a>
 */
public class TestDataGridIterationStatus extends AbstractDataGridTest {

    @FindByJQuery("table.rf-dg[id$=richDataGrid]")
    private GridWithStates dataGrid;

    @Drone
    private WebDriver browser;

    public TestDataGridIterationStatus() throws JAXBException {
        super();
    }

    @Override
    public RichFacesDataGrid<? extends GridRecordInterface> getDataGrid() {
        return dataGrid;
    }

    @Override
    public String getComponentTestPagePath() {
        return "richDataGrid/iterationStatus.xhtml";
    }

    @Test
    @CoversAttributes("iterationStatusVar")
    public void testAll() {
        testBegin();
        testCount();
        testEnd();
        testEven();
        testFirst();
        testIndex();
        testLast();
        testRowCount();
    }

    public void testBegin() {
        for (int i = 0; i < dataGrid.getNumberOfRecords(); i += 2) {
            WebElement beginLocator = browser.findElement(ByJQuery.selector("span[id$=statusBegin]:eq(" + i + ")"));
            assertEquals(beginLocator.getText(), "begin=0", "The iteration status property <begin> doesn't match (item: " + i + ").");
        }
    }

    public void testCount() {
        for (int i = 0; i < dataGrid.getNumberOfRecords(); i += 2) {
            WebElement propertyLocator = browser.findElement(ByJQuery.selector("span[id$=statusCount]:eq(" + i + ")"));
            String expected = "count=" + (i + 1);
            assertEquals(propertyLocator.getText(), expected, "The iteration status property <count> doesn't match (item: " + i + ").");
        }
    }

    public void testEnd() {
        String expected = "end=" + (dataGrid.getNumberOfRecords() - 1);
        for (int i = 0; i < dataGrid.getNumberOfRecords(); i += 2) {
            WebElement propertyLocator = browser.findElement(ByJQuery.selector("span[id$=statusEnd]:eq(" + i + ")"));
            assertEquals(propertyLocator.getText(), expected, "The iteration status property <end> doesn't match (item: " + i + ").");
        }
    }

    public void testEven() {
        for (int i = 0; i < dataGrid.getNumberOfRecords(); i += 2) {
            boolean expected = false;
            if (i % 2 == 1) {
                expected = true;
            }
            WebElement propertyLocator = browser.findElement(ByJQuery.selector("span[id$=statusEven]:eq(" + i + ")"));
            assertEquals(propertyLocator.getText(), "even=" + Boolean.toString(expected), "The iteration status property <even> doesn't match (item: " + i + ").");
        }
    }

    public void testFirst() {
        for (int i = 0; i < dataGrid.getNumberOfRecords(); i += 2) {
            boolean expected = false;
            if (i == 0) {
                expected = true;
            }
            WebElement propertyLocator = browser.findElement(ByJQuery.selector("span[id$=statusFirst]:eq(" + i + ")"));
            assertEquals(propertyLocator.getText(), "first=" + Boolean.toString(expected), "The iteration status property <first> doesn't match (item: " + i + ").");
        }
    }

    public void testIndex() {
        for (int i = 0; i < dataGrid.getNumberOfRecords(); i += 2) {
            WebElement propertyLocator = browser.findElement(ByJQuery.selector("span[id$=statusIndex]:eq(" + i + ")"));
            String expected = "index=" + i;
            assertEquals(propertyLocator.getText(), expected, "The iteration status property <index> doesn't match (item: " + i + ").");
        }
    }

    public void testLast() {
        for (int i = 0; i < dataGrid.getNumberOfRecords(); i += 2) {
            boolean expected = false;
            if (i == dataGrid.getNumberOfRecords() - 1) {
                expected = true;
            }
            WebElement propertyLocator = browser.findElement(ByJQuery.selector("span[id$=statusLast]:eq(" + i + ")"));
            assertEquals(propertyLocator.getText(), "last=" + Boolean.toString(expected), "The iteration status property <last> doesn't match (item: " + i + ").");
        }
    }

    public void testRowCount() {
        String expected = "rowCount=" + dataGrid.getNumberOfRecords();
        for (int i = 0; i < dataGrid.getNumberOfRecords(); i += 2) {
            WebElement propertyLocator = browser.findElement(ByJQuery.selector("span[id$=statusRowCount]:eq(" + i + ")"));
            assertEquals(propertyLocator.getText(), expected, "The iteration status property <rowCount> doesn't match (item: " + i + ").");
        }
    }
}
