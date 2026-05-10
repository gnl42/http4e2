package me.glindholm.plugin.http4e2.httpclient.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import me.glindholm.plugin.http4e2.httpclient.ui.HdViewPart;

public class ImportMenuAction extends Action implements IMenuCreator {

    private final Shell shell;
    private final HdViewPart view;
    private Menu menu;
    private Action http4eAction;
    private Action liveHeadersAction;
    private Action packetAction;

    public ImportMenuAction(final Shell shell, final HdViewPart view) {
        this.shell = shell;
        this.view = view;
        menu = null;
        setToolTipText("Import HTTP packets");
        setMenuCreator(this);
    }

    @Override
    public void dispose() {
        // action is reused, can be called several times.
        if (menu != null) {
            menu.dispose();
            menu = null;
        }
    }

    @Override
    public Menu getMenu(final Menu parent) {
        return null;
    }

    @Override
    public Menu getMenu(final Control parent) {
        if (menu != null) {
            menu.dispose();
        }
        menu = new Menu(parent);
        addActionToMenu(menu, getPacketAction());
        addActionToMenu(menu, getHTTP4eAction());
        addActionToMenu(menu, getLiveHeadersAction());
        return menu;
    }

    protected void addActionToMenu(final Menu parent, final Action action) {
        final ActionContributionItem item = new ActionContributionItem(action);
        item.fill(parent, -1);
    }

    private Action getLiveHeadersAction() {
        if (liveHeadersAction == null) {
            liveHeadersAction = new ImportLiveHttpHeadersAction(shell, view);
        }
        return liveHeadersAction;
    }

    private Action getPacketAction() {
        if (packetAction == null) {
            packetAction = new ImportPacketAction(shell, view);
        }
        return packetAction;
    }

    private Action getHTTP4eAction() {
        if (http4eAction == null) {
            http4eAction = new ImportHTTP4eAction(shell, view);
        }
        return http4eAction;
    }

    @Override
    public void run() {
        getHTTP4eAction().run();
    }
}
