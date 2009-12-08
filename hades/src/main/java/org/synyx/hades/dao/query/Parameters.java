/*
 * Copyright 2002-2008 the original author or authors.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Query;

import org.springframework.util.Assert;
import org.synyx.hades.dao.Param;
import org.synyx.hades.domain.Pageable;
import org.synyx.hades.domain.Sort;
import org.synyx.hades.util.ClassUtils;


/**
 * Abstracts method parameters that have to be bound to query parameters or
 * applied to the query independently.
 * 
 * @author Oliver Gierke - gierke@synyx.de
 */
class Parameters {

    @SuppressWarnings("unchecked")
    public static final List<Class<?>> TYPES =
            Arrays.asList(Pageable.class, Sort.class);

    private static final String PARAM_ON_SPECIAL =
            String.format(
                    "You must not user @%s on a parameter typed %s or %s",
                    Param.class.getSimpleName(),
                    Pageable.class.getSimpleName(), Sort.class.getSimpleName());

    private static final String ALL_OR_NOTHING =
            String
                    .format(
                            "Either use @%s "
                                    + "on all parameters except %s and %s typed once, or none at all!",
                            Param.class.getSimpleName(), Pageable.class
                                    .getSimpleName(), Sort.class
                                    .getSimpleName());

    private int pageableIndex;
    private int sortIndex;

    private Method method;

    private Object[] parameters;


    /**
     * Creates a new instance of {@link Parameters}.
     * 
     * @param method
     * @param parameters
     */
    public Parameters(Method method, Object... parameters) {

        List<Class<?>> types = Arrays.asList(method.getParameterTypes());
        this.pageableIndex = types.indexOf(Pageable.class);
        this.sortIndex = types.indexOf(Sort.class);

        this.method = method;
        this.parameters = parameters;

        assertEitherAllParamAnnotatedOrNone();
    }


    /**
     * Binds the parameters to the given {@link Query}.
     * 
     * @param query
     * @return
     */
    public Query bind(Query query) {

        int position = 1;

        for (Object parameter : parameters) {

            if (ClassUtils.isOfType(parameter, TYPES)) {
                continue;
            }

            int parameterIndex = position - 1;

            if (isNamedParameter(parameterIndex)) {
                query.setParameter(getParameterName(parameterIndex), parameter);
                position++;
            } else {
                query.setParameter(position++, parameter);
            }
        }

        return query;
    }


    /**
     * Returns the name of the parameter (through {@link Param} annotation) or
     * null if none can be found.
     * 
     * @param position
     * @return
     */
    public String getParameterName(int position) {

        if (position > method.getParameterAnnotations().length - 1) {
            return null;
        }

        Annotation[] parameterAnnotations =
                method.getParameterAnnotations()[position];

        for (Annotation annotation : parameterAnnotations) {

            if (annotation instanceof Param) {
                return ((Param) annotation).value();
            }
        }

        return null;
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

        if (!hasPageableParameter() || getPageable() == null) {
            return result;
        }

        result.setFirstResult(getPageable().getFirstItem());
        result.setMaxResults(getPageable().getPageSize());

        return result;
    }


    /**
     * Applies sorting to the given query string.
     * 
     * @param query
     * @return
     */
    public String applySorting(String query) {

        return QueryUtils.applySorting(query, getSort());
    }


    /**
     * Returns the {@link Pageable} of the parameters, if available. Returns
     * {@code null} otherwise.
     * 
     * @return
     */
    public Pageable getPageable() {

        if (!hasPageableParameter()) {
            return null;
        }

        return (Pageable) parameters[pageableIndex];
    }


    /**
     * Returns whether the method the {@link Parameters} was created for
     * contains a {@link Pageable} argument. This does not include a check if an
     * actual parameter was provided, so {@link #getPageable()} might return
     * {@literal null} even in case this method returns {@literal true}.
     * 
     * @return
     */
    public boolean hasPageableParameter() {

        return pageableIndex != -1;
    }


    /**
     * Returns whether the method the {@link Parameters} was created for
     * contains a {@link Sort} argument. This does not include a check if an
     * actual parameter was provided, so {@link #getSort()} might return
     * {@literal null} even in case this method returns {@literal true}.
     * 
     * @return
     */
    public boolean hasSortParameter() {

        return sortIndex != -1;
    }


    /**
     * Returns the sort instance to be used for query creation. Will use a
     * {@link Sort} parameter if available or the {@link Sort} contained in a
     * {@link Pageable} if available. Returns {@code null} if no {@link Sort}
     * can be found.
     * 
     * @return
     */
    Sort getSort() {

        if (hasSortParameter()) {
            return (Sort) parameters[sortIndex];
        }

        if (hasPageableParameter() && getPageable() != null) {
            return getPageable().getSort();
        }

        return null;
    }


    /**
     * Returns whether the parameter with the given index is a special
     * parameter.
     * 
     * @see #TYPES
     * @param index
     * @return
     */
    public final boolean isSpecialParameter(int index) {

        return TYPES.contains(method.getParameterTypes()[index]);
    }


    /**
     * Returns whether the parameter with the given index is annotated with
     * {@link Param}.
     * 
     * @param index
     * @return
     */
    public boolean isNamedParameter(int index) {

        return null != getParameterName(index);
    }


    /**
     * Asserts that either all of the non special parameters ({@link Pageable},
     * {@link Sort}) are annotated with {@link Param} or none of them is.
     * 
     * @param method
     */
    private void assertEitherAllParamAnnotatedOrNone() {

        boolean annotationFound = false;

        for (int index = 0; index < method.getParameterTypes().length; index++) {

            if (isSpecialParameter(index)) {
                Assert.isTrue(!isNamedParameter(index), PARAM_ON_SPECIAL);
                continue;
            }

            if (isNamedParameter(index)) {
                Assert.isTrue(annotationFound || index == 0, ALL_OR_NOTHING);
                annotationFound = true;
            } else {
                Assert.isTrue(!annotationFound, ALL_OR_NOTHING);
            }
        }
    }
}