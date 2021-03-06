/**
 * Copyright 2012 Ekito - http://www.ekito.fr/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ekito.simpleKML.model;

import java.util.List;

import org.simpleframework.xml.ElementList;

/**
 * A {@link StyleMap} maps between two different Styles. Typically a {@link StyleMap} element is used to provide separate normal and highlighted styles for a placemark, so that the highlighted version appears when the user mouses over the icon in Google Earth.
 */
public class StyleMap extends StyleSelector {

	/** The pair list. */
	@ElementList(entry="Pair", inline=false, type=Pair.class, required=false)
	private List<Pair> pairList;

	/**
	 * Gets the pair.
	 *
	 * @return the pair
	 */
	public List<Pair> getPairList() {
		return this.pairList;
	}

	/**
	 * Sets the pair.
	 *
	 * @param pair the new pair
	 */
	public void setPairList(List<Pair> pairList) {
		this.pairList = pairList;
	}
}
