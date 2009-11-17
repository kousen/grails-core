/*
 * Copyright 2004-2005 the original author or authors.
 *
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
 */
package org.codehaus.groovy.grails.web.binding;

import org.codehaus.groovy.grails.commons.metaclass.LazyMetaPropertyMap;

import java.util.List;

/**
 * Extends the default implementation and does data binding
 *
 * @author Graeme Rocher
 * @since 1.1
 */
public class DataBindingLazyMetaPropertyMap extends LazyMetaPropertyMap{
    /**
     * Constructs the map
     *
     * @param o The object to inspect
     */
    public DataBindingLazyMetaPropertyMap(Object o) {
        super(o);
    }

    public Object put(Object propertyName, Object propertyValue) {
        if(propertyName instanceof List) {
            DataBindingUtils.bindObjectToInstance(getInstance(),propertyValue, (List)propertyName,null,null);
            return null;
        }
        else {
            return super.put(propertyName, propertyValue);    
        }

    }
}