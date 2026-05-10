package me.glindholm.plugin.http4e2.httpclient.ui;

import java.util.function.Consumer;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import me.glindholm.plugin.http4e2.httpclient.core.CoreContext;
import me.glindholm.plugin.http4e2.httpclient.core.CoreObjects;
import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.client.misc.BusinessJob;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ProxyItem;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.FolderView;
import me.glindholm.plugin.http4e2.httpclient.ui.actions.AddTabAction;
import me.glindholm.plugin.http4e2.httpclient.ui.actions.AuthDialog;
import me.glindholm.plugin.http4e2.httpclient.ui.actions.DuplicateTabAction;
import me.glindholm.plugin.http4e2.httpclient.ui.actions.ExportMenuAction;
import me.glindholm.plugin.http4e2.httpclient.ui.actions.HelpDropDownAction;
import me.glindholm.plugin.http4e2.httpclient.ui.actions.ImportMenuAction;
import me.glindholm.plugin.http4e2.httpclient.ui.actions.ParameterizeAction;
import me.glindholm.plugin.http4e2.httpclient.ui.actions.ProxyAction;

/**
 * Pure E4 view part — no E3 compatibility layer.
 * <p>
 * Registered via {@code <e4view>} in {@code plugin.xml}. Eclipse lists it in
 * the "Show View" dialog and opens it through {@link EPartService} automatically.
 * <p>
 * The {@link #init(Composite)} method is intentionally <em>not</em> named
 * {@code createPartControl} to prevent the E3 compatibility bridge from trying
 * to invoke it as an {@link org.eclipse.ui.IViewPart} lifecycle method.
 */
public class HdViewPart {

    private static final String PLUGIN_ID = "me.glindholm.plugin.http4e2.ui";
    private static final String ICON_BASE  = "platform:/plugin/" + PLUGIN_ID + "/icons/";

    public static final String ID = "me.glindholm.plugin.http4e2.ui.HdViewPart";

    @Inject
    @Optional
    private MPart part;

    private HdContentViewer viewer;
    private Shell shell;
    private MDirectToolItem proxyToolItem;

    public FolderView getFolderView() {
        return viewer.getFolderView();
    }

    @PostConstruct
    public void init(final Composite parent) {
        buildUI(parent);
    }

    public void createPartControl(final Composite parent) {
        buildUI(parent);
    }

    private void buildUI(final Composite parent) {
        if (viewer != null) {
            return; // already built (both paths fired)
        }
        try {
            shell = parent.getShell();

            // Populate the E4 model toolbar (shown in the part title bar)
            buildPartToolbar(shell);

            parent.setLayout(new GridLayout(1, false));

            viewer = new HdContentViewer(parent);
            viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

            final org.eclipse.swt.graphics.Cursor waitCursor =
                    parent.getDisplay().getSystemCursor(SWT.CURSOR_WAIT);
            Job.getJobManager().addJobChangeListener(new JobChangeAdapter() {
                @Override
                public void running(final IJobChangeEvent event) {
                    if (event.getJob().belongsTo(BusinessJob.FAMILY)) {
                        parent.getDisplay().asyncExec(() -> setCursorOnTree(parent, waitCursor));
                    }
                }
                @Override
                public void done(final IJobChangeEvent event) {
                    if (event.getJob().belongsTo(BusinessJob.FAMILY)) {
                        parent.getDisplay().asyncExec(() -> setCursorOnTree(parent, null));
                    }
                }
            });

            if (part != null) {
                final String originalLabel = part.getLabel();
                Job.getJobManager().addJobChangeListener(new JobChangeAdapter() {
                    @Override
                    public void running(final IJobChangeEvent event) {
                        if (event.getJob().belongsTo(BusinessJob.FAMILY)) {
                            parent.getDisplay().asyncExec(() -> part.setLabel("⟳ " + originalLabel));
                        }
                    }
                    @Override
                    public void done(final IJobChangeEvent event) {
                        if (event.getJob().belongsTo(BusinessJob.FAMILY)) {
                            parent.getDisplay().asyncExec(() -> part.setLabel(originalLabel));
                        }
                    }
                });
            }

            final CoreContext ctx = CoreContext.getContext();

            final Consumer<ItemModel> authOpener = itemModel -> {
                try {
                    new AuthDialog(shell, itemModel).open();
                } catch (final Exception ex) {
                    ExceptionHandler.handle(ex);
                }
            };
            ctx.putObject(CoreObjects.AUTH_DIALOG_OPENER, authOpener);

            final ProxyItem proxyItem = (ProxyItem) ctx.getObject(CoreObjects.PROXY_ITEM);
            fireProxyEnable(proxyItem != null && proxyItem.isProxy());

            parent.addDisposeListener(e -> viewer.doDispose());

        } catch (final Exception e) {
            e.printStackTrace();
            parent.setLayout(new org.eclipse.swt.layout.FillLayout());
            final Label lbl = new Label(parent, SWT.WRAP);
            lbl.setText("HTTP4e2 failed to load: " + e.getClass().getSimpleName() + ": " + e.getMessage()
                    + (e.getCause() != null ? "\nCaused by: " + e.getCause() : ""));
            parent.layout(true);
        }
    }

    /** Populates the E4 model toolbar that Eclipse renders in the part title bar. */
    private void buildPartToolbar(final Shell sh) {
        if (part == null) {
            return;
        }

        MToolBar mToolBar = part.getToolbar();
        if (mToolBar == null) {
            mToolBar = MMenuFactory.INSTANCE.createToolBar();
            part.setToolbar(mToolBar);
        }

        // Clear any items that may have been persisted in the workbench model
        // from a previous session; we always rebuild from scratch so that handler
        // object references belong to the current instance.
        mToolBar.getChildren().clear();

        final HdViewPart self = this;

        // New tab
        final MDirectToolItem addTabItem = MMenuFactory.INSTANCE.createDirectToolItem();
        addTabItem.setIconURI(ICON_BASE + "tab-new.png");
        addTabItem.setTooltip("Open a new tab");
        addTabItem.setObject(new SimpleHandler(() -> new AddTabAction(self).run()));
        mToolBar.getChildren().add(addTabItem);

        // Duplicate tab
        final MDirectToolItem dupItem = MMenuFactory.INSTANCE.createDirectToolItem();
        dupItem.setIconURI(ICON_BASE + "duplicate.png");
        dupItem.setTooltip("Duplicate tab");
        dupItem.setObject(new SimpleHandler(() -> new DuplicateTabAction(self).run()));
        mToolBar.getChildren().add(dupItem);

        // Parameterize
        final MDirectToolItem paramItem = MMenuFactory.INSTANCE.createDirectToolItem();
        paramItem.setIconURI(ICON_BASE + "params.png");
        paramItem.setTooltip("Parameterize your call");
        paramItem.setObject(new SimpleHandler(() -> new ParameterizeAction(self).run()));
        mToolBar.getChildren().add(paramItem);

        // Import (dropdown)
        final ImportMenuAction importMenuAction = new ImportMenuAction(sh, this);
        final MDirectToolItem importItem = MMenuFactory.INSTANCE.createDirectToolItem();
        importItem.setIconURI(ICON_BASE + "import.png");
        importItem.setTooltip("Import HTTP packets");
        importItem.setObject(new DropdownHandler(sh, importMenuAction));
        mToolBar.getChildren().add(importItem);

        // Export (dropdown)
        final ExportMenuAction exportMenuAction = new ExportMenuAction(sh, this);
        final MDirectToolItem exportItem = MMenuFactory.INSTANCE.createDirectToolItem();
        exportItem.setIconURI(ICON_BASE + "export.png");
        exportItem.setTooltip("Export HTTP call to different languages");
        exportItem.setObject(new DropdownHandler(sh, exportMenuAction));
        mToolBar.getChildren().add(exportItem);

        // Proxy
        proxyToolItem = MMenuFactory.INSTANCE.createDirectToolItem();
        proxyToolItem.setIconURI(ICON_BASE + "proxy.png");
        proxyToolItem.setTooltip("Proxy Connection");
        proxyToolItem.setObject(new SimpleHandler(() -> new ProxyAction(self).run()));
        mToolBar.getChildren().add(proxyToolItem);

        // Help (dropdown)
        final HelpDropDownAction helpDropDown = new HelpDropDownAction(sh);
        final MDirectToolItem helpItem = MMenuFactory.INSTANCE.createDirectToolItem();
        helpItem.setIconURI(ICON_BASE + "help.png");
        helpItem.setTooltip("About, License");
        helpItem.setObject(new DropdownHandler(sh, helpDropDown));
        mToolBar.getChildren().add(helpItem);
    }

    // -------------------------------------------------------------------------
    // E4 toolbar handler helpers
    // -------------------------------------------------------------------------

    /** Handler for simple (non-dropdown) toolbar buttons. */
    public static final class SimpleHandler {
        private final Runnable action;
        public SimpleHandler(final Runnable action) { this.action = action; }
        @Execute public void execute() { action.run(); }
    }

    /**
     * Handler for dropdown toolbar buttons: shows the {@link IMenuCreator}'s SWT
     * menu at the current cursor location.
     */
    public static final class DropdownHandler {
        private final Shell shell;
        private final org.eclipse.jface.action.Action menuAction;

        public DropdownHandler(final Shell shell, final org.eclipse.jface.action.Action menuAction) {
            this.shell = shell;
            this.menuAction = menuAction;
        }

        @Execute
        public void execute(final Display display) {
            final Menu menu = ((IMenuCreator) menuAction).getMenu(shell);
            if (menu != null) {
                final org.eclipse.swt.graphics.Point loc = display.getCursorLocation();
                menu.setLocation(loc.x, loc.y);
                menu.setVisible(true);
            }
        }
    }

    // -------------------------------------------------------------------------

    public void setFocus() {
        if (viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed()) {
            viewer.getControl().setFocus();
        }
    }

    @PreDestroy
    public void dispose() {
        // ToolBarManager removed; nothing to dispose here for the model toolbar
    }

    private static void setCursorOnTree(final Composite root, final org.eclipse.swt.graphics.Cursor cursor) {
        if (root == null || root.isDisposed()) {
            return;
        }
        root.setCursor(cursor);
        for (final Control child : root.getChildren()) {
            child.setCursor(cursor);
            if (child instanceof Composite) {
                setCursorOnTree((Composite) child, cursor);
            }
        }
    }

    public void fireAuthEnable(final boolean enabled) {
        // per-tab; no global toolbar action
    }

    public void fireProxyEnable(final boolean enabled) {
        if (proxyToolItem == null) {
            return;
        }
        proxyToolItem.setIconURI(ICON_BASE + (enabled ? "proxy_enabled.png" : "proxy.png"));
    }

    public Shell getShell() {
        return shell;
    }
}