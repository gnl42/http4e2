package me.glindholm.plugin.http4e2.httpclient.ui.actions;

import java.io.StringWriter;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;


import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreImages;
import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ModelEvent;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.FolderView;
import me.glindholm.plugin.http4e2.httpclient.core.util.BaseUtils;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;
import me.glindholm.plugin.http4e2.httpclient.ui.HdViewPart;

public class ExportJavaHttp5Action extends Action {

    private final Shell shell;
    private final HdViewPart view;
    private Menu fMenu;

    public ExportJavaHttp5Action(final Shell shell, final HdViewPart view) {
        this.shell = shell;
        this.view = view;
        fMenu = null;
        setToolTipText("Export call as Apache HttpComponents 5 script");
        setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.JAVA)));
        setText("     Apache HttpComponents 5");
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
            final FolderView folderView = view.getFolderView();
            final ItemModel iModel = folderView.getModel().getItemModel(folderView.getSelectionItemHash());
            iModel.fireExecute(new ModelEvent(ModelEvent.EXPORT, iModel));

            final StringWriter writer = new StringWriter();
            BaseUtils.writeJavaHttpClient5(iModel, writer);

            final ExportDialog dialog = new ExportDialog(shell, view.getFolderView(), "Apache HTTP Components 5.x call", "Your call exported as Java Apache HTTP Components 5.x call.",
                    writer.toString());
            dialog.open();
        } catch (final Exception e) {
            ExceptionHandler.handle(e);
        }
    }

}
