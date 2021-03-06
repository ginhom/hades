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

package org.synyx.hades.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.synyx.hades.domain.Page;
import org.synyx.hades.domain.Pageable;
import org.synyx.hades.domain.Sort;
import org.synyx.hades.domain.Specification;


/**
 * Interface for generic CRUD operations on a DAO for a specific type.
 * 
 * @author Eberhard Wolff
 * @author Oliver Gierke
 */
@Transactional(readOnly = true)
public interface GenericDao<T, PK extends Serializable> {

    /**
     * Saves a given entity. Use the returned instance for further operations as
     * the save operation might have changed the entity instance completely.
     * 
     * @param entity
     * @return the saved entity
     */
    @Transactional
    T save(final T entity);


    /**
     * @param entities
     * @return
     */
    @Transactional
    List<T> save(final Collection<? extends T> entities);


    /**
     * Saves an entity and flushes changes instantly to the database.
     * 
     * @param entity
     * @return the saved entity
     */
    @Transactional
    T saveAndFlush(final T entity);


    /**
     * Retrives an entity by it's primary key.
     * 
     * @param primaryKey
     * @return the entity with the given primary key or {@code null} if none
     *         found
     * @throws IllegalArgumentException if primaryKey is {@code null}
     */
    T readByPrimaryKey(final PK primaryKey);


    /**
     * Returns whether an entity with the given id exists.
     * 
     * @param primaryKey
     * @return true if an entity with the given id exists, alse otherwise
     * @throws IllegalArgumentException if primaryKey is {@code null}
     */
    boolean exists(final PK primaryKey);


    /**
     * Returns all instances of the type.
     * 
     * @return all entities
     */
    List<T> readAll();


    /**
     * Returns all entities sorted by the given options.
     * 
     * @param sort
     * @return all entities sorted by the given options
     */
    List<T> readAll(final Sort sort);


    /**
     * Returns a paged list of entities meeting the paging restriction provided
     * in the {@code Pageable} object.
     * 
     * @param pageable
     * @return a page of entities
     */
    Page<T> readAll(final Pageable pageable);


    /**
     * Returns all entities matching the given {@link Specification}.
     * 
     * @param spec
     * @return
     */
    List<T> readAll(final Specification<T> spec);


    /**
     * Returns a {@link Page} of entities matching the given
     * {@link Specification}.
     * 
     * @param spec
     * @param pageable
     * @return
     */
    Page<T> readAll(final Specification<T> spec, final Pageable pageable);


    /**
     * Returns the number of entities available.
     * 
     * @return the number of entities
     */
    Long count();


    /**
     * Returns the number of instances that the given {@link Specification} will
     * return.
     * 
     * @param spec the {@link Specification} to count instances for
     * @return the number of instances
     */
    Long count(final Specification<T> spec);


    /**
     * Deletes a given entity.
     * 
     * @param entity
     */
    @Transactional
    void delete(final T entity);


    /**
     * Deletes the given entities.
     * 
     * @param entities
     */
    @Transactional
    void delete(final Collection<? extends T> entities);


    /**
     * Deletes all entities managed by the DAO.
     */
    @Transactional
    void deleteAll();


    /**
     * Flushes all pending changes to the database.
     */
    @Transactional
    void flush();
}
