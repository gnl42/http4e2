package me.glindholm.plugin.http4e2.httpclient.ui.actions;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreImages;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ModelEvent;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.FolderView;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;
import me.glindholm.plugin.http4e2.httpclient.core.util.shared.ExportJavaViewer;

public class ExportDialog extends TitleAreaDialog {

    private final FolderView folderView;
    private final String title;
    private final String titleMessage;
    private final String source;

    public ExportDialog(final Shell shell, final FolderView folderView, final String title, final String titleMessage, final String source) {
        super(shell);
        setTitleImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.LOGO_DIALOG));
        this.folderView = folderView;
        this.title = title;
        this.titleMessage = titleMessage;
        this.source = source;
    }

    @Override
    protected Control createContents(final Composite parent) {
        final Control contents = super.createContents(parent);
        setTitle(title);
        setMessage(titleMessage);
        return contents;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);

        try {
            final ItemModel itemModel = folderView.getModel().getItemModel(folderView.getSelectionItemHash());
            itemModel.fireExecute(new ModelEvent(ModelEvent.EXPORT, itemModel));

            final ExportJavaViewer example = new ExportJavaViewer(composite);
            example.open(source);

        } catch (final Exception e) {
            setErrorMessage(e.getLocalizedMessage());
        }

        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

}