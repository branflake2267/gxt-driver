package org.senchalabs.gwt.gwtdriver.gxt.models;

/*
 * #%L
 * Sencha GXT classes for gwt-driver
 * %%
 * Copyright (C) 2012 - 2013 Sencha Labs
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.senchalabs.gwt.gwtdriver.by.ByNearestWidget;
import org.senchalabs.gwt.gwtdriver.by.ByWidget;
import org.senchalabs.gwt.gwtdriver.by.CheatingByChained;
import org.senchalabs.gwt.gwtdriver.by.FasterByChained;
import org.senchalabs.gwt.gwtdriver.gxt.models.Button.ButtonFinder;
import org.senchalabs.gwt.gwtdriver.gxt.models.Window.WindowFinder;
import org.senchalabs.gwt.gwtdriver.models.GwtWidget;
import org.senchalabs.gwt.gwtdriver.models.GwtWidget.ForWidget;
import org.senchalabs.gwt.gwtdriver.models.GwtWidgetFinder;

@ForWidget(com.sencha.gxt.widget.core.client.Window.class)
public class Window extends GwtWidget<WindowFinder> {
	public Window(WebDriver driver, WebElement element) {
		super(driver, element);
	}

	public WebElement getHeaderElement() {
		return getElement().findElement(new FasterByChained(By.xpath(".//*"),
				new ByWidget(getDriver(), com.sencha.gxt.widget.core.client.Header.class)));
	}

	public boolean isCollapsed() {
		return getHeaderElement().getSize().getHeight() == getElement().getSize().getHeight();
	}
	
	public boolean isVisible() {
	  return getElement().isDisplayed();
	}

	public static class WindowFinder extends GwtWidgetFinder<Window> {
		private String heading;
		private boolean atTop = false;

		public WindowFinder withHeading(String heading) {
			this.heading = heading;
			return this;
		}

		@Override
		public WindowFinder withDriver(WebDriver driver) {
			return (WindowFinder) super.withDriver(driver);
		}

		public WindowFinder atTop() {
			atTop = true;
			return this;
		}

		@Override
		public Window done() {
			WebElement elt = null;
			if (heading != null) {
				// Within the body tag, there are several Windows.
				String escaped = escapeToString(heading);
				List<WebElement> windows = driver.findElements(new ByChained(
						By.xpath("//body/*"),
						new ByWidget(driver, com.sencha.gxt.widget.core.client.Window.class)
				));

				// For each one, select the first Header (the window's own header),
				// and test it for the text. If it matches, return the first surrounding window
				for (WebElement window : windows) {
					for (WebElement possibleHeader : window.findElements(By.xpath(".//*"))) {
						// Using a try/catch here to get CheatingByChained for a faster lookup as
						// this will throw an exception if it doesn't match so we can move on to the next,
						// or it will return right away when it matches all rules
						try {
							possibleHeader.findElement(new CheatingByChained(
									new ByWidget(driver, com.sencha.gxt.widget.core.client.Header.class),
									By.xpath(".//*[contains(text(), " + escaped + ")]"),
									new ByNearestWidget(driver, com.sencha.gxt.widget.core.client.Window.class)
							));
							elt = window;
							break;
						} catch (NoSuchElementException ex) {
							// Ignore, continue on to the next element in the list
						}
					}
					if (elt != null) {
						// Found a matching window, give up and go home
						break;
					}

				}
			} else if (atTop) {
				List<WebElement> allWindows = driver.findElements(new ByChained(By.xpath("//body/*"),
						new ByWidget(driver, com.sencha.gxt.widget.core.client.Window.class)));
				Collections.sort(allWindows, new Comparator<WebElement>() {
					public int compare(WebElement o1, WebElement o2) {
						return Integer.parseInt(o2.getCssValue("z-index")) - Integer.parseInt(o1.getCssValue("z-index"));
					}
				});
				elt = allWindows.get(0);
			}
			return new Window(driver, elt);
		}
	}
}
