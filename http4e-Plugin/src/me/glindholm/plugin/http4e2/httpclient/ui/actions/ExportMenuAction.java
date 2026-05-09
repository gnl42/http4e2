package me.glindholm.plugin.http4e2.httpclient.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreImages;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;
import me.glindholm.plugin.http4e2.httpclient.ui.HdViewPart;

public class ExportMenuAction extends Action implements IMenuCreator {

    private final Shell shell;
    private final HdViewPart view;
    private Menu menu;
    private Action javaHttp3Action;
    private Action javaHttp4Action;
    private Action javaHttp5Action;
    private Action javaHttp21Action;
    private Action jmeterAction;
    private Action prototypeAction;
    private Action jqueryAction;
    private Action jsAction;
    private Action csAction;
    private Action vbAction;
    private Action flexAction;
    private Action rubyAction;
    private Action phpAction;
    private Action ocAction;
    private Action pythonAction;
    private Action http4eAction;
    private Action emptyAction;
    private PrintAction printAction;

    public ExportMenuAction(final Shell shell, final HdViewPart view) {
        this.shell = shell;
        this.view = view;
        menu = null;
        setToolTipText("Export HTTP call to different languages");
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
        // addActionToMenu(fMenu, new HowtoAction(view, "How-to, How-to"));
        // addActionToMenu(fMenu, new ContributeAction(view, "Contribute to
        // Plugin"));
        // new MenuItem(fMenu, SWT.SEPARATOR);
        addActionToMenu(menu, getHTTP4eAction());
        addActionToMenu(menu, getJMeterAction());
        addActionToMenu(menu, getPrintAction());
        if (true) {
            new MenuItem(menu, SWT.SEPARATOR);
            addActionToMenu(menu, getEmptyAction());
            addActionToMenu(menu, getJavaHttp21Action());
            addActionToMenu(menu, getJavaHttp5Action());
            addActionToMenu(menu, getJSAction());
            addActionToMenu(menu, getPrototypeAction());
            addActionToMenu(menu, getJqueryAction());
            addActionToMenu(menu, getFlexAction());
            addActionToMenu(menu, getCsAction());
            addActionToMenu(menu, getVbAction());
            addActionToMenu(menu, getRubyAction());
            addActionToMenu(menu, getPythonAction());
            addActionToMenu(menu, getPHPAction());
            addActionToMenu(menu, getOCAction());
        }
        return menu;
    }

    protected void addActionToMenu(final Menu parent, final Action action) {
        final ActionContributionItem item = new ActionContributionItem(action);
        item.fill(parent, -1);
    }

    private Action getPrintAction() {
        printAction = new PrintAction(shell, view, "Print /Save PDF");
        printAction.setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.PRINT)));
        return printAction;
    }

    private Action getJavaHttp5Action() {
        if (javaHttp5Action == null) {
            javaHttp5Action = new ExportJavaHttp5Action(shell, view);
        }
        return javaHttp5Action;
    }

    private Action getJavaHttp21Action() {
        if (javaHttp21Action == null) {
            javaHttp21Action = new ExportJavaHttp21Action(shell, view);
        }
        return javaHttp21Action;
    }

    private Action getJMeterAction() {
        if (jmeterAction == null) {
            jmeterAction = new ExportJMeterAction(shell, view);
        }
        return jmeterAction;
    }

    private Action getPrototypeAction() {
        if (prototypeAction == null) {
            prototypeAction = new ExportAjaxPrototypeAction(shell, view);
        }
        return prototypeAction;
    }

    private Action getJqueryAction() {
        if (jqueryAction == null) {
            jqueryAction = new ExportAjaxJQueryAction(shell, view);
        }
        return jqueryAction;
    }

    private Action getCsAction() {
        if (csAction == null) {
            csAction = new ExportCsAction(shell, view);
        }
        return csAction;
    }

    private Action getVbAction() {
        if (vbAction == null) {
            vbAction = new ExportVbAction(shell, view);
        }
        return vbAction;
    }

    private Action getFlexAction() {
        if (flexAction == null) {
            flexAction = new ExportFlexAction(shell, view);
        }
        return flexAction;
    }

    private Action getRubyAction() {
        if (rubyAction == null) {
            rubyAction = new ExportRubyAction(shell, view);
        }
        return rubyAction;
    }

    private Action getPHPAction() {
        if (phpAction == null) {
            phpAction = new ExportPHPAction(shell, view);
        }
        return phpAction;
    }

    private Action getOCAction() {
        if (ocAction == null) {
            ocAction = new ExportCocoaAction(shell, view);
        }
        return ocAction;
    }

    private Action getHTTP4eAction() {
        if (http4eAction == null) {
            http4eAction = new ExportHTTP4eAction(shell, view);
        }
        return http4eAction;
    }

    private Action getJSAction() {
        if (jsAction == null) {
            jsAction = new ExportAjaxJSAction(shell, view);
        }
        return jsAction;
    }

    private Action getEmptyAction() {
        if (emptyAction == null) {
            emptyAction = new ExportEmptyAction();
        }
        return emptyAction;
    }

    private Action getPythonAction() {
        if (pythonAction == null) {
            pythonAction = new ExportPythonAction(shell, view);
        }
        return pythonAction;
    }

    @Override
    public void run() {
        getHTTP4eAction().run();
    }
}
