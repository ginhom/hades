/*
 * Copyright 2008-2010 the original author or authors.
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

package org.synyx.hades.domain;

import java.util.Iterator;
import java.util.List;


/**
 * A page is a sublist of a list of objects. It allows gain information about
 * the position of it in the containing entire list.
 * 
 * @author Oliver Gierke
 * @param <T>
 */
public interface Page<T> extends Iterable<T> {

    /**
     * Returns the number of the current page. Is zero based and less that
     * {@code Page#getTotalPages()}.
     * 
     * @return the number of the current page
     */
    int getNumber();


    /**
     * Returns the size of the page.
     * 
     * @return the size of the page
     */
    int getSize();


    /**
     * Returns the number of total pages.
     * 
     * @return the number of toral pages
     */
    int getTotalPages();


    /**
     * Returns the number of elements currently on this page.
     * 
     * @return the number of elements currently on this page
     */
    int getNumberOfElements();


    /**
     * Returns the total amount of elements.
     * 
     * @return the total amount of elements
     */
    long getTotalElements();


    /**
     * Returns if there is a previous page.
     * 
     * @return if there is a previous page
     */
    boolean hasPreviousPage();


    /**
     * Returns if there is a next page.
     * 
     * @return if there is a next page
     */
    boolean hasNextPage();


    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    Iterator<T> iterator();


    /**
     * Returns the page content as {@link List}.
     * 
     * @return
     */
    List<T> asList();


    /**
     * Returns the sorting parameters for the page.
     * 
     * @return
     */
    Sort getSort();
}