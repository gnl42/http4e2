package me.glindholm.plugin.http4e2.httpclient.ui;

import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

/**
 * E4 model <em>processor</em> that removes stale CompatibilityView entries for
 * the http4e2 view from the saved application model before rendering starts.
 *
 * <h3>Why this is needed</h3>
 * When the plugin was previously registered via {@code org.eclipse.ui.views},
 * Eclipse wrote the view into {@code e4workbench.xmi} as a
 * {@code CompatibilityView} part (including a
 * {@code originalCompatibilityViewClass} persisted-state key set to
 * {@code HdViewPart}).  On the next start the compatibility layer reads that
 * key, synthesises a {@code ViewDescriptor}, calls
 * {@code ViewDescriptor.createView()}, and tries to cast {@code HdViewPart} to
 * {@code IViewPart} — causing the {@link ClassCastException}.
 *
 * <h3>Why a <em>processor</em> and not an <em>addon</em></h3>
 * A model processor with {@code beforefragment="false"} runs after all model
 * fragments have been merged but <em>before</em> the Eclipse renderer creates
 * any widgets.  An addon's {@code @PostConstruct} is called later, after the
 * compatibility layer has already started creating {@code CompatibilityPart}
 * instances — too late to prevent the cast.
 */
public class Http4e2ModelFixer {

    private static final String COMPAT_URI  = "CompatibilityView";
    private static final String POJO_URI    =
            "bundleclass://me.glindholm.plugin.http4e2.ui/"
            + "me.glindholm.plugin.http4e2.httpclient.ui.HdViewPart";

    /**
     * Called by the E4 DI framework during model processing, before rendering.
     */
    @Execute
    public void fixStaleCompatibilityParts(final MApplication application,
                                           final EModelService modelService) {
        final List<MPart> parts = modelService.findElements(
                application, HdViewPart.ID, MPart.class, null);

        for (final MPart part : parts) {
            final String uri = part.getContributionURI();
            if (uri != null && uri.contains(COMPAT_URI)) {
                // Replace the E3 CompatibilityView contribution with the
                // direct bundleclass URI so the E4 renderer instantiates
                // HdViewPart via DI without going through IViewPart.
                part.setContributionURI(POJO_URI);
                // Also clear the persisted state that the compatibility layer
                // uses to synthesise a ViewDescriptor at runtime.
                part.getPersistedState().remove("originalCompatibilityViewClass");
                part.getPersistedState().remove("originalCompatibilityViewBundle");
            }
        }
    }
}