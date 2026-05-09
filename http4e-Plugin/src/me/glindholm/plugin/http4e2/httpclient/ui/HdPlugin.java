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
package me.glindholm.plugin.http4e2.httpclient.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

import me.glindholm.plugin.http4e2.httpclient.ui.preferences.EclipsePreferenceStore;

import me.glindholm.plugin.http4e2.httpclient.core.CoreContext;
import me.glindholm.plugin.http4e2.httpclient.core.CoreObjects;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;
import me.glindholm.plugin.http4e2.httpclient.ui.preferences.PreferenceConstants;

/**
 * Pure E4 bundle activator — no dependency on org.eclipse.ui or the E3
 * compatibility layer.
 *
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public class HdPlugin implements BundleActivator, UIConstants {

    private static HdPlugin plugin;
    private ResourceBundle resourceBundle;
    private IPreferenceStore preferenceStore;

    public static String getResourceString(final String key) {
        final ResourceBundle bundle = HdPlugin.getDefault().getResourceBundle();
        try {
            return bundle != null ? bundle.getString(key) : key;
        } catch (final MissingResourceException e) {
            return key;
        }
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public HdPlugin() {
        plugin = this;
    }

    /** Returns the plugin-scoped OSGi preference node (InstanceScope). */
    public IEclipsePreferences getPreferenceNode() {
        return InstanceScope.INSTANCE.getNode(PLUGIN_ID);
    }

    /**
     * Returns a JFace {@link IPreferenceStore} backed by the OSGi preference
     * node.  Suitable for use from preference pages and field editors.
     */
    public IPreferenceStore getPreferenceStore() {
        if (preferenceStore == null) {
            preferenceStore = new EclipsePreferenceStore(PLUGIN_ID);
        }
        return preferenceStore;
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        try {
            resourceBundle = ResourceBundle.getBundle(HdPlugin.class.getName());
        } catch (final MissingResourceException x) {
            resourceBundle = null;
        }

        final IEclipsePreferences prefs = getPreferenceNode();
        int size = prefs.getInt(PreferenceConstants.P_PAYLOAD_VIEW_SIZE, 0);
        if (size < 10) {
            size = 50;
            prefs.putInt(PreferenceConstants.P_PAYLOAD_VIEW_SIZE, size);
            try {
                prefs.flush();
            } catch (final BackingStoreException e) {
                // non-fatal — default will be used
            }
        }
        CoreContext.getContext().putObject(CoreObjects.RESPONSE_VIEW_SIZE, size);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        cleanupUserDirectoryPackets(".REQ.txt");
        cleanupUserDirectoryPackets(".RESP.txt");
        plugin = null;
    }

    private static void cleanupUserDirectoryPackets(final String ext) {
        final FilenameFilter filter = (dir, name) -> name.endsWith(ext);
        final File dir = new File(CoreContext.PRODUCT_USER_DIR);
        final String[] files = dir.list(filter);
        if (files == null) {
            return;
        }
        for (final String name : files) {
            new File(dir, name).delete();
        }
    }

    public static HdPlugin getDefault() {
        return plugin;
    }

    public static ImageDescriptor getImageDescriptor(final String path) {
        return ResourceUtils.getImageDescriptor(PLUGIN_ID, path);
    }
}