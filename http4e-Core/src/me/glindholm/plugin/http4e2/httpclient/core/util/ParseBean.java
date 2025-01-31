/*
 *  Copyright 2017 Eclipse HttpClient (http4e) https://nextinterfaces.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.glindholm.plugin.http4e2.httpclient.core.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
class ParseBean {

    public String key;
    public Set<String> values = new HashSet<>();

    public ParseBean(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void addValue(final String val) {
        values.add(val);
    }

    public Collection<String> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "Param{" + "key=" + key + ",values=" + values + "}";
    }

}
