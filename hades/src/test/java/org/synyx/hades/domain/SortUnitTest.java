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

import static org.junit.Assert.*;

import org.junit.Test;


/**
 * Unit test for {@link Sort}.
 * 
 * @author Oliver Gierke
 */
public class SortUnitTest {

    /**
     * Asserts that the class applies the default sort order if no order or
     * {@code null} was provided.
     * 
     * @throws Exception
     */
    @Test
    public void appliesDefaultForOrder() throws Exception {

        assertEquals(Sort.DEFAULT_ORDER, new Sort("foo").iterator().next()
                .getOrder());
        assertEquals(Sort.DEFAULT_ORDER, new Sort((Order) null, "foo")
                .iterator().next().getOrder());
    }


    /**
     * Asserts that the class rejects {@code null} as properties array.
     * 
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void preventsNullProperties() throws Exception {

        new Sort(Order.ASCENDING, (String[]) null);
    }


    /**
     * Asserts that the class rejects {@code null} values in the properties
     * array.
     * 
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void preventsNullProperty() throws Exception {

        new Sort(Order.ASCENDING, (String) null);
    }


    /**
     * Asserts that the class rejects empty strings in the properties array.
     * 
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void preventsEmptyProperty() throws Exception {

        new Sort(Order.ASCENDING, "");
    }


    /**
     * Asserts that the class rejects no properties given at all.
     * 
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void preventsNoProperties() throws Exception {

        new Sort(Order.ASCENDING);
    }
}
