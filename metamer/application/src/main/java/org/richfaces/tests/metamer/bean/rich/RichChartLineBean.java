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
package org.richfaces.tests.metamer.bean.rich;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.richfaces.component.UIChart;
import org.richfaces.model.PlotClickEvent;
import org.richfaces.tests.metamer.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed bean for rich:chart, contains event handlers and initializes data for chart.
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@ManagedBean(name = "chartBean")
@SessionScoped
public class RichChartLineBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger logger;
    private Attributes attributes;

    // string msg from server side event
    private String msg;

    private List<Country> countries;

    /**
     * Initializes the managed bean.
     */
    @PostConstruct
    public void init() {
        logger = LoggerFactory.getLogger(getClass());
        logger.debug("initializing bean " + getClass().getName());

        attributes = Attributes.getComponentAttributesFromFacesConfig(UIChart.class, getClass());
        attributes.setAttribute("title", "Countries by carbon dioxide emissions per capital");
        attributes.setAttribute("styleClass", "graf");
        attributes.setAttribute("onplotclick", "log(event)");
        attributes.setAttribute("onplothover", "hover(event)");
        attributes.setAttribute("onmouseout", "clear()");
        attributes.setAttribute("rendered", true);
        attributes.setAttribute("zoom", "true");

        attributes.remove("plotClickListener");

        // init server side msg
        msg = "no server-side event";

        // initialize all charts with data
        Country usa = new Country("USA");
        usa.put(1990, 19.1);
        usa.put(1991, 18.9);
        usa.put(1992, 18.6);
        usa.put(1993, 19.5);
        usa.put(1994, 19.5);
        usa.put(1995, 19.3);
        usa.put(1996, 19.4);
        usa.put(1997, 19.7);
        usa.put(1998, 19.5);
        usa.put(1999, 19.5);
        usa.put(2000, 20.0);

        Country china = new Country("China");
        china.put(1990, 2.2);
        china.put(1991, 2.2);
        china.put(1992, 2.3);
        china.put(1993, 2.4);
        china.put(1994, 2.6);
        china.put(1995, 2.7);
        china.put(1996, 2.8);
        china.put(1997, 2.8);
        china.put(1998, 2.7);
        china.put(1999, 2.6);
        china.put(2000, 2.7);

        Country japan = new Country("Japan");
        japan.put(1990, 9.4);
        japan.put(1991, 9.4);
        japan.put(1992, 9.5);
        japan.put(1993, 9.4);
        japan.put(1994, 9.9);
        japan.put(1995, 9.9);
        japan.put(1996, 10.1);
        japan.put(1997, 10.1);
        japan.put(1998, 9.7);
        japan.put(1999, 9.5);
        japan.put(2000, 9.7);

        Country russia = new Country("Russia");
        russia.put(1992, 14);
        russia.put(1993, 12.8);
        russia.put(1994, 10.9);
        russia.put(1995, 10.5);
        russia.put(1996, 10.4);
        russia.put(1997, 10);
        russia.put(1998, 9.6);
        russia.put(1999, 9.7);
        russia.put(2000, 9.8);

        countries = new LinkedList<Country>();
        countries.add(usa);
        countries.add(china);
        countries.add(japan);
        countries.add(russia);
    }

    public void handler(PlotClickEvent event) {
        msg = event.toString();
    }

    public String getMsg() {
        return msg;
    }

    public List<Country> getCountries() {
        return countries;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public static class Country {

        private final String name;
        // CO2 production year-> metric tons per capital
        private final List<Record> data;

        public Country(String name) {
            this.name = name;
            this.data = new LinkedList<Record>();
        }

        public void put(int year, double tons) {
            getData().add(new Record(year, tons));
        }

        public List<Record> getData() {
            return data;
        }

        public String getName() {
            return name;
        }
    }

    public static class Record {

        private final int year;
        private final double tons;

        public Record(int year, double tons) {
            this.year = year;
            this.tons = tons;
        }

        public double getTons() {
            return tons;
        }

        public int getYear() {
            return year;
        }
    }
}
