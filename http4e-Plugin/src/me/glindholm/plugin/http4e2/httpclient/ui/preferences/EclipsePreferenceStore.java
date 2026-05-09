package me.glindholm.plugin.http4e2.httpclient.ui.preferences;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferenceStore;
import org.osgi.service.prefs.BackingStoreException;

/**
 * A JFace {@link PreferenceStore} backed by an {@link IEclipsePreferences} node.
 * <p>
 * On construction all existing preference keys are loaded from the OSGi
 * preference node into the in-memory JFace store. On {@link #save()} they
 * are written back and flushed, so standard JFace {@link org.eclipse.jface.preference.FieldEditorPreferencePage}
 * machinery continues to work without any dependency on {@code org.eclipse.ui}.
 */
public class EclipsePreferenceStore extends PreferenceStore {

    private final IEclipsePreferences prefs;

    /**
     * Creates a store backed by the {@link InstanceScope} node for
     * {@code pluginId}.
     */
    public EclipsePreferenceStore(final String pluginId) {
        this.prefs = InstanceScope.INSTANCE.getNode(pluginId);
        loadFromNode();
    }

    /** Loads all current values from the OSGi preference node. */
    private void loadFromNode() {
        try {
            for (final String key : prefs.keys()) {
                final String value = prefs.get(key, null);
                if (value != null) {
                    // store as default so "Restore Defaults" reflects the persisted value
                    setDefault(key, value);
                    setValue(key, value);
                }
            }
        } catch (final BackingStoreException e) {
            // non-fatal — start with empty store
        }
    }

    /**
     * Persists all values back to the OSGi preference node and flushes to disk.
     * Called automatically by JFace field-editor pages on Apply/OK.
     */
    @Override
    public void save() {
        for (final String key : preferenceNames()) {
            prefs.put(key, getString(key));
        }
        try {
            prefs.flush();
        } catch (final BackingStoreException e) {
            // non-fatal
        }
    }
}