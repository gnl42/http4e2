package me.glindholm.plugin.http4e2.httpclient.ui;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

/**
 * Opens the http4e2 view.
 * <p>
 * Registered as an E3 command handler via {@code org.eclipse.ui.handlers}.
 * The {@link #execute(EPartService)} method uses {@code @Execute} so the E4
 * DI bridge injects {@link EPartService} automatically when the command fires —
 * unlike {@code @Inject} fields, which are not set when the class is
 * instantiated via {@code IConfigurationElement.createExecutableExtension()}.
 */
public class ShowHttp4e2ViewHandler {

    @Execute
    public void execute(final EPartService partService) {
        partService.showPart(HdViewPart.ID, PartState.ACTIVATE);
    }
}