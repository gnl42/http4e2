package me.glindholm.plugin.http4e2.httpclient.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.Item;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.FolderView;
import me.glindholm.plugin.http4e2.httpclient.core.util.Translator;
import me.glindholm.plugin.http4e2.httpclient.ui.HdViewPart;

public class ImportPacketAction extends Action {

    private final Shell shell;
    private final HdViewPart view;
    private Menu fMenu;

    public ImportPacketAction(final Shell shell, final HdViewPart view) {
        this.shell = shell;
        this.view = view;
        fMenu = null;
        setToolTipText("Import raw HTTP packet");
        setText("Import raw HTTP packet");
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
            final ImportDialog dialog = new ImportDialog(shell);
            final MouseAdapter okListener = new MouseAdapter() {
                @Override
                public void mouseUp(final MouseEvent e) {
                    Item item = new Item();
                    try {
                        item = Translator.httppacketToItem(dialog.getText());
                    } catch (final Exception exc) {
                        exc.printStackTrace();
                    }
                    final FolderView folderView = view.getFolderView();
                    final ItemModel iModel = new ItemModel(folderView.getModel(), item);
                    view.getFolderView().buildTab(iModel);
                }
            };
            dialog.setOkListener(okListener);
            dialog.open();
        } catch (final Exception e) {
            ExceptionHandler.handle(e);
        }
    }
}