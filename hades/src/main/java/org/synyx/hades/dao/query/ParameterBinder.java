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
package org.synyx.hades.dao.query;

import javax.persistence.Query;

import org.springframework.util.Assert;
import org.synyx.hades.domain.Pageable;
import org.synyx.hades.domain.Sort;


/**
 * {@link ParameterBinder} is used to bind method parameters to a {@link Query}.
 * This is usually done whenever a {@link HadesQuery} is executed.
 * 
 * @author Oliver Gierke
 */
class ParameterBinder {

    private final Parameters parameters;
    private final Object[] values;


    /**
     * Creates a new {@link ParameterBinder}.
     * 
     * @param parameters
     * @param values
     */
    public ParameterBinder(Parameters parameters, Object... values) {

        Assert.notNull(parameters);
        Assert.notNull(values);

        Assert.isTrue(parameters.getNumberOfParameters() == values.length,
                "Invalid number of parameters given!");

        this.parameters = parameters;
        this.values = values;
    }


    /**
     * Returns the {@link Pageable} of the parameters, if available. Returns
     * {@code null} otherwise.
     * 
     * @return
     */
    public Pageable getPageable() {

        if (!parameters.hasPageableParameter()) {
            return null;
        }

        return (Pageable) values[parameters.getPageableIndex()];
    }


    /**
     * Returns the sort instance to be used for query creation. Will use a
     * {@link Sort} parameter if available or the {@link Sort} contained in a
     * {@link Pageable} if available. Returns {@code null} if no {@link Sort}
     * can be found.
     * 
     * @return
     */
    public Sort getSort() {

        if (parameters.hasSortParameter()) {
            return (Sort) values[parameters.getSortIndex()];
        }

        if (parameters.hasPageableParameter() && getPageable() != null) {
            return getPageable().getSort();
        }

        return null;
    }


    /**
     * Binds the parameters to the given {@link Query}.
     * 
     * @param query
     * @return
     */
    public Query bind(Query query) {

        int methodParameterPosition = 0;

        for (Parameter parameter : parameters) {

            if (parameter.isBindable()) {

                Object value = values[methodParameterPosition];

                if (parameter.isNamedParameter()) {
                    query.setParameter(parameter.getParameterName(), value);
                } else {
                    query.setParameter(parameter.getParameterPosition(), value);
                }
            }

            methodParameterPosition++;
        }

        return query;
    }


    /**
     * Binds the parameters to the given query and applies special parameter
     * types (e.g. pagination).
     * 
     * @param query
     * @return
     */
    public Query bindAndPrepare(Query query) {

        Query result = bind(query);

        if (!parameters.hasPageableParameter() || getPageable() == null) {
            return result;
        }

        result.setFirstResult(getPageable().getFirstItem());
        result.setMaxResults(getPageable().getPageSize());

        return result;
    }
}
