/*
 *  Copyright 2017 Eclipse HttpClient (http4e) https://nextinterfaces.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.glindholm.plugin.http4e2.httpclient.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;

import me.glindholm.plugin.http4e2.httpclient.ui.HdPlugin;

/**
 * Preference page for HTTP4e2 settings.
 * Pure E4 — no org.eclipse.ui dependency.
 *
 * @author andreaszbschmidt
 */
public class HTTPClientPreferencePage extends FieldEditorPreferencePage {

    public HTTPClientPreferencePage() {
        super(GRID);
        setPreferenceStore(HdPlugin.getDefault().getPreferenceStore());
        setDescription("HTTP4e2 Preferences");
    }

    @Override
    public void createFieldEditors() {
        addField(new ProxyItemListEditor(PreferenceConstants.P_PROXY_LIST, "&Proxylist", getFieldEditorParent()));
    }
}