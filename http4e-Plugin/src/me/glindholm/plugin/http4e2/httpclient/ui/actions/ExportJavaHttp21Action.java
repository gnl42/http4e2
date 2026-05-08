package me.glindholm.plugin.http4e2.httpclient.ui.actions;

import java.io.StringWriter;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreImages;
import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ModelEvent;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.FolderView;
import me.glindholm.plugin.http4e2.httpclient.core.util.BaseUtils;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;
import me.glindholm.plugin.http4e2.httpclient.ui.HdViewPart;

public class ExportJavaHttp21Action extends Action {

    private final ViewPart view;
    private Menu fMenu;

    public ExportJavaHttp21Action(final ViewPart view) {
        this.view = view;
        fMenu = null;
        setToolTipText("Export call as Java 21 java.net.http.HttpClient script");
        setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.JAVA)));
        setText("     Java 21 HttpClient (java.net.http)");
    }

    public void dispose() {
        if (fMenu != null) {
            fMenu.dispose();
            fMenu = null;
        }
    }

    @Override
    public void run() {
        try {
            final FolderView folderView = ((HdViewPart) view).getFolderView();
            final ItemModel iModel = folderView.getModel().getItemModel(folderView.getSelectionItemHash());
            iModel.fireExecute(new ModelEvent(ModelEvent.EXPORT, iModel));

            final StringWriter writer = new StringWriter();
            BaseUtils.writeJavaHttp21(iModel, writer);

            final ExportDialog dialog = new ExportDialog(view, "Java 21 HttpClient call", "Your call exported as Java 21 java.net.http.HttpClient call.",
                    writer.toString());
            dialog.open();
        } catch (final Exception e) {
            ExceptionHandler.handle(e);
        }
    }

}
