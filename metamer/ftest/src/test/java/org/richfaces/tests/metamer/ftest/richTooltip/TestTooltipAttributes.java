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
package org.richfaces.tests.metamer.ftest.richTooltip;

import static javax.faces.event.PhaseId.APPLY_REQUEST_VALUES;
import static javax.faces.event.PhaseId.RENDER_RESPONSE;
import static javax.faces.event.PhaseId.RESTORE_VIEW;

import static org.richfaces.tests.metamer.ftest.extension.configurator.use.annotation.ValuesFrom.FROM_ENUM;
import static org.richfaces.tests.metamer.ftest.extension.configurator.use.annotation.ValuesFrom.FROM_FIELD;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.richfaces.TooltipMode;
import org.richfaces.component.SwitchType;
import org.richfaces.fragment.common.Actions;
import org.richfaces.fragment.common.Event;
import org.richfaces.fragment.common.Locations;
import org.richfaces.fragment.common.Utils;
import org.richfaces.fragment.tooltip.TextualRichFacesTooltip;
import org.richfaces.tests.metamer.ftest.AbstractWebDriverTest;
import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
import org.richfaces.tests.metamer.ftest.extension.attributes.coverage.annotations.CoversAttributes;
import org.richfaces.tests.metamer.ftest.extension.configurator.skip.annotation.Skip;
import org.richfaces.tests.metamer.ftest.extension.configurator.templates.annotation.Templates;
import org.richfaces.tests.metamer.ftest.extension.configurator.use.annotation.UseWithField;
import org.richfaces.tests.metamer.ftest.webdriver.Attributes;
import org.richfaces.tests.metamer.ftest.webdriver.MetamerPage;
import org.richfaces.tests.metamer.ftest.webdriver.MetamerPage.WaitRequestType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
 * @author <a href="mailto:jstefek@redhat.com">Jiri Stefek</a>
 */
public class TestTooltipAttributes extends AbstractWebDriverTest {

    private final Integer[] delays = { 500, 1200, 1900 };
    private final Attributes<TooltipAttributes> tooltipAttributes = getAttributes();

    private Integer delay;
    private TooltipMode mode;

    @Page
    private TooltipPage page;

    @Override
    public String getComponentTestPagePath() {
        return "richTooltip/simple.xhtml";
    }

    @BeforeMethod(groups = "smoke")
    public void setupAttributes() {
        tooltipAttributes.set(TooltipAttributes.hideEvent, "mouseout");
        tooltipAttributes.set(TooltipAttributes.showEvent, "click");
        tooltip().advanced().setHideEvent(Event.MOUSEOUT);
        tooltip().advanced().setShowEvent(Event.CLICK);
    }

    private void showAndMoveTooltip(final boolean tooltipWillMove) {
        final int moveBy = 20;
        final int tolerance = 5;
        final LocationWrapper locations = new LocationWrapper();
        tooltip().show().hide();// acquire id of tooltip
        new Actions(driver).moveToElement(page.getPanel()).click()// show tooltip
            .addAction(new Action() {
                @Override
                public void perform() {
                    tooltip().advanced().waitUntilTooltipIsVisible().perform();
                    locations.setLocations(Utils.getLocations(tooltip().advanced().getTooltipElement()));
                }
            }).moveByOffset(-moveBy, 0).addAction(new Action() {
                @Override
                public void perform() {
                    Utils.tolerantAssertLocationsEquals(tooltip().advanced().getTooltipElement(), tooltipWillMove ? locations
                            .getLocations().moveAllBy(-moveBy, 0) : locations.getLocations(), tolerance, tolerance, "");
                }
            }).perform();
    }

    @Test
    @CoversAttributes("attached")
    public void testAttached() {
        tooltipAttributes.set(TooltipAttributes.attached, true);
        tooltip().show().hide();

        tooltipAttributes.set(TooltipAttributes.attached, false);
        try {
            tooltip().show();
            fail("Not attached tooltip cannot be invoked by triggering show event on parent component");
        } catch (TimeoutException ok) {
        }
        tooltip().advanced().waitUntilTooltipIsVisible();
        // when not attached, tooltip can be invoked by JS API
        page.getJsAPIshowClick().click();
        assertTrue(tooltip().advanced().isVisible());
    }

    @Test
    @CoversAttributes("data")
    public void testData() {
        tooltipAttributes.set(TooltipAttributes.mode, TooltipMode.ajax);
        testData(new Action() {
            @Override
            public void perform() {
                tooltip().show();
            }
        });
    }

    @Test
    @CoversAttributes("dir")
    @Templates(value = "plain")
    public void testDir() {
        testDir(tooltip().show().advanced().getTooltipElement());
    }

    @Test
    @CoversAttributes("direction")
    @Templates("plain")
    @UseWithField(field = "positioning", valuesFrom = FROM_ENUM, value = "")
    public void testDirection() {
        tooltip().show().hide();// workaround: first the tooltip will show on the corner of the panel, then always in the middle
        tooltipAttributes.set(TooltipAttributes.followMouse, Boolean.FALSE);
        testDirection(new ShowElementAndReturnAction() {
            @Override
            public WebElement perform() {
                new Actions(driver).moveToElement(page.getPanel()).build().perform();
                return tooltip().advanced().getTooltipElement();
            }
        });
    }

    @Test(groups = "smoke")
    @CoversAttributes("followMouse")
    @Templates(value = "plain")
    public void testFollowMouse() {
        tooltipAttributes.set(TooltipAttributes.followMouse, Boolean.FALSE);
        showAndMoveTooltip(Boolean.FALSE);

        tooltipAttributes.set(TooltipAttributes.followMouse, Boolean.TRUE);
        showAndMoveTooltip(Boolean.TRUE);
    }

    @Test
    @CoversAttributes("hideDelay")
    @UseWithField(field = "delay", valuesFrom = FROM_FIELD, value = "delays")
    @Templates("plain")
    public void testHideDelay() {
        new MenuDelayTester().testHideDelay(driver.findElement(By.cssSelector("[id$='tooltip']")), delay, Event.MOUSEOUT, page.getPanel());
    }

    @Test
    @CoversAttributes("hideEvent")
    @Templates(value = "plain")
    public void testHideEvent() {
        tooltipAttributes.set(TooltipAttributes.followMouse, "false");// for stabilization

        Event event = Event.MOUSEOUT;
        tooltipAttributes.set(TooltipAttributes.hideEvent, event.toString());
        tooltip().advanced().setHideEvent(event);
        tooltip().show().hide();

        event = Event.DBLCLICK;
        tooltipAttributes.set(TooltipAttributes.hideEvent, event.toString());
        tooltip().advanced().setHideEvent(event);
        tooltip().show();
        new Actions(driver).moveToElement(page.getPanel(), 0, 0).doubleClick().perform();// hide
        tooltip().advanced().waitUntilTooltipIsNotVisible().perform();
    }

    @Test
    @CoversAttributes("horizontalOffset")
    @Templates(value = "plain")
    public void testHorizontalOffset() {
        tooltip().show().hide();// workaround: first the tooltip will show on the corner of the panel, then always in the middle
        testHorizontalOffset(new ShowElementAndReturnAction() {
            @Override
            public WebElement perform() {
                return tooltip().show().advanced().getTooltipElement();
            }
        });
    }

    @Test
    @Skip
    @CoversAttributes("jointPoint")
    @Templates("plain")
    @UseWithField(field = "positioning", valuesFrom = FROM_ENUM, value = "")
    @IssueTracking(value = "https://issues.jboss.org/browse/RF-13760")
    public void testJointPoint() {
        tooltip().show().hide();// workaround: first the tooltip will show on the corner of the panel, then always in the middle
        tooltipAttributes.set(TooltipAttributes.followMouse, Boolean.FALSE);
        testJointPoint(page.getPanel().getSize().getWidth(), page.getPanel().getSize().getHeight(),
            (new ShowElementAndReturnAction() {
                @Override
                public WebElement perform() {
                    new Actions(driver).moveToElement(page.getPanel()).build().perform();
                    return tooltip().advanced().getTooltipElement();
                }
            }));
    }

    @Test
    @CoversAttributes("lang")
    @Templates(value = "plain")
    public void testLang() {
        testLang(tooltip().show().advanced().getTooltipElement());
    }

    @Test(groups = "smoke")
    @CoversAttributes("layout")
    @Templates(value = "plain")
    public void testLayout() {
        tooltipAttributes.set(TooltipAttributes.layout, "inline");
        assertEquals(tooltip().show().advanced().getTooltipElement().getTagName(), "span");
        tooltipAttributes.set(TooltipAttributes.layout, "block");
        assertEquals(tooltip().show().advanced().getTooltipElement().getTagName(), "div");
    }

    @Test
    public void testLifecycle() {
        tooltipAttributes.set(TooltipAttributes.mode, TooltipMode.ajax);
        MetamerPage.waitRequest(tooltip(), WaitRequestType.XHR).show();
        page.assertPhases(RESTORE_VIEW, APPLY_REQUEST_VALUES, RENDER_RESPONSE);
    }

    @Test
    @CoversAttributes("limitRender")
    public void testLimitRender() {
        testLimitRenderWithSwitchTypeOrMode(new Action() {
            @Override
            public void perform() {
                tooltip().show();
            }
        });
    }

    @Test
    @CoversAttributes("mode")
    @UseWithField(field = "mode", valuesFrom = FROM_ENUM, value = "")
    public void testMode() {
        tooltipAttributes.set(TooltipAttributes.mode, mode);
        (mode.equals(TooltipMode.ajax) ? Graphene.guardAjax(tooltip()) : Graphene.guardNoRequest(tooltip())).show();
        Graphene.guardNoRequest(tooltip()).hide();
    }

    @Test
    @CoversAttributes("onbeforedomupdate")
    public void testOnbeforedomupdate() {
        tooltipAttributes.set(TooltipAttributes.mode, SwitchType.ajax);
        testFireEvent("onbeforedomupdate", new Action() {
            @Override
            public void perform() {
                Graphene.guardAjax(tooltip()).show();
            }
        });
    }

    @Test
    @CoversAttributes("onbeforehide")
    public void testOnbeforehide() {
        testFireEvent("onbeforehide", new Action() {
            @Override
            public void perform() {
                tooltip().show().hide();
            }
        });
    }

    @Test
    @CoversAttributes("onbeforeshow")
    public void testOnbeforeshow() {
        testFireEvent("onbeforeshow", new Action() {
            @Override
            public void perform() {
                tooltip().show();
            }
        });
    }

    @Test
    @CoversAttributes("onbegin")
    public void testOnbegin() {
        tooltipAttributes.set(TooltipAttributes.mode, SwitchType.ajax);
        testFireEvent("onbegin", new Action() {
            @Override
            public void perform() {
                Graphene.guardAjax(tooltip()).show();
            }
        });
    }

    @Test
    @CoversAttributes("onclick")
    @Templates(value = "plain")
    public void testOnclick() {
        tooltipAttributes.set(TooltipAttributes.hideEvent, Event.KEYPRESS.getEventName());
        testFireEvent("onclick", new Action() {
            @Override
            public void perform() {
                tooltip().show().advanced().getTooltipElement().click();
            }
        });
    }

    @Test
    @CoversAttributes("oncomplete")
    public void testOncomplete() {
        tooltipAttributes.set(TooltipAttributes.mode, SwitchType.ajax);
        testFireEvent("oncomplete", new Action() {
            @Override
            public void perform() {
                Graphene.guardAjax(tooltip()).show();
            }
        });
    }

    @Test
    @CoversAttributes("ondblclick")
    @Templates(value = "plain")
    public void testOndblclick() {
        tooltipAttributes.set(TooltipAttributes.hideEvent, Event.KEYPRESS.getEventName());
        testFireEvent("ondblclick", new Action() {
            @Override
            public void perform() {
                new Actions(driver).doubleClick(tooltip().show().advanced().getTooltipElement()).perform();
            }
        });
    }

    @Test
    @CoversAttributes("onhide")
    public void testOnhide() {
        testFireEvent("onhide", new Action() {
            @Override
            public void perform() {
                tooltip().show().hide();
            }
        });
    }

    @Test
    @CoversAttributes("onmousedown")
    @Templates(value = "plain")
    public void testOnmousedown() {
        tooltipAttributes.set(TooltipAttributes.hideEvent, Event.KEYPRESS.getEventName());
        testFireEvent("onmousedown", new Action() {
            @Override
            public void perform() {
                new Actions(driver).click(tooltip().show().advanced().getTooltipElement()).perform();
            }
        });
    }

    @Test
    @CoversAttributes("onmousemove")
    @Templates(value = "plain")
    public void testOnmousemove() {
        tooltipAttributes.set(TooltipAttributes.hideEvent, Event.KEYPRESS.getEventName());
        testFireEvent("onmousemove", new Action() {
            @Override
            public void perform() {
                new Actions(driver).triggerEventByWD(Event.MOUSEMOVE, tooltip().show().advanced().getTooltipElement())
                    .perform();
            }
        });
    }

    @Test
    @CoversAttributes("onmouseout")
    @Templates(value = "plain")
    public void testOnmouseout() {
        testFireEvent("onmouseout", new Action() {
            @Override
            public void perform() {
                new Actions(driver).triggerEventByWD(Event.MOUSEOUT, tooltip().show().advanced().getTooltipElement()).perform();
            }
        });
    }

    @Test
    @CoversAttributes("onmouseover")
    @Templates(value = "plain")
    public void testOnmouseover() {
        testFireEvent("onmouseover", new Action() {
            @Override
            public void perform() {
                new Actions(driver).triggerEventByWD(Event.MOUSEOVER, tooltip().show().advanced().getTooltipElement())
                    .perform();
            }
        });
    }

    @Test
    @CoversAttributes("onmouseup")
    @Templates(value = "plain")
    public void testOnmouseup() {
        tooltipAttributes.set(TooltipAttributes.hideEvent, Event.KEYPRESS.getEventName());
        testFireEvent("onmouseup", new Action() {
            @Override
            public void perform() {
                new Actions(driver).click(tooltip().show().advanced().getTooltipElement()).perform();
            }
        });
    }

    @Test
    @CoversAttributes("onshow")
    public void testOnshow() {
        testFireEvent("onshow", new Action() {
            @Override
            public void perform() {
                tooltip().show();
            }
        });
    }

    @Test
    @CoversAttributes("render")
    @Templates(value = "plain")
    public void testRender() {
        tooltipAttributes.set(TooltipAttributes.mode, SwitchType.ajax);
        testRenderWithSwitchTypeOrMode(new Action() {
            @Override
            public void perform() {
                tooltip().show();
            }
        });
    }

    @Test
    @CoversAttributes("rendered")
    @Templates(value = "plain")
    public void testRendered() {
        tooltipAttributes.set(TooltipAttributes.rendered, false);
        try {
            tooltip().show();
            fail("Tooltip cannot be displayed when redered = false");
        } catch (TimeoutException ok) {
        }
    }

    @Test
    @CoversAttributes({ "onbegin", "onbeforedomupdate", "oncomplete" })
    public void testRequestEventHandlers() {
        tooltipAttributes.set(TooltipAttributes.mode, TooltipMode.ajax);
        testRequestEventsBefore("begin", "beforedomupdate", "complete");
        MetamerPage.waitRequest(tooltip(), WaitRequestType.XHR).show();
        testRequestEventsAfter("begin", "beforedomupdate", "complete");
    }

    @Test
    @CoversAttributes("showDelay")
    @UseWithField(field = "delay", valuesFrom = FROM_FIELD, value = "delays")
    @Templates("plain")
    public void testShowDelay() {
        new MenuDelayTester().testShowDelay(driver.findElement(By.cssSelector("[id$='tooltip']")), delay, Event.CLICK, page.getPanel());
    }

    @Test
    @CoversAttributes("showEvent")
    @Templates(value = "plain")
    public void testShowEvent() {
        for (Event event : new Event[]{ Event.CLICK, Event.DBLCLICK }) {
            tooltipAttributes.set(TooltipAttributes.showEvent, event.toString());
            tooltip().advanced().setShowEvent(event);
            tooltip().show().hide();
        }
    }

    @Test
    @CoversAttributes("status")
    public void testStatus() {
        tooltipAttributes.set(TooltipAttributes.mode, SwitchType.ajax);
        testStatus(new Action() {
            @Override
            public void perform() {
                tooltip().show();
            }
        });
    }

    @Test
    @CoversAttributes("style")
    @Templates(value = "plain")
    public void testStyle() {
        super.testStyle(tooltip().show().advanced().getTooltipElement());
    }

    @Test
    @CoversAttributes("styleClass")
    @Templates(value = "plain")
    public void testStyleClass() {
        super.testStyleClass(tooltip().show().advanced().getTooltipElement());
    }

    @Test
    @CoversAttributes("title")
    @Templates(value = "plain")
    public void testTitle() {
        super.testTitle(tooltip().show().advanced().getTooltipElement());
    }

    @Test
    @CoversAttributes("verticalOffset")
    @Templates(value = "plain")
    public void testVerticalOffset() {
        tooltip().show().hide();// workaround: first the tooltip will show on the corner of the panel, then always in the middle
        testVerticalOffset(new ShowElementAndReturnAction() {
            @Override
            public WebElement perform() {
                return tooltip().show().advanced().getTooltipElement();
            }
        });
    }

    @Test
    @CoversAttributes("zindex")
    @Templates(value = "plain")
    public void testZindex() {
        tooltipAttributes.set(TooltipAttributes.zindex, 10);
        assertEquals(tooltip().show().advanced().getTooltipElement().getCssValue("z-index"), "10");
    }

    private TextualRichFacesTooltip tooltip() {
        return page.getTooltip();
    }

    private class LocationWrapper {

        private Locations locations;

        public Locations getLocations() {
            return locations;
        }

        public void setLocations(Locations locations) {
            this.locations = locations;
        }
    }
}
