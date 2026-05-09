package me.glindholm.plugin.http4e2.httpclient.ui;

import java.util.function.Consumer;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreContext;
import me.glindholm.plugin.http4e2.httpclient.core.CoreImages;
import me.glindholm.plugin.http4e2.httpclient.core.CoreObjects;
import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.client.misc.BusinessJob;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ProxyItem;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.FolderView;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;
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

    public static final String ID = "me.glindholm.plugin.http4e2.ui.HdViewPart";

    @Inject
    @Optional
    private MPart part;

    private HdContentViewer viewer;
    private Shell shell;
    private ToolBarManager tbm;
    private ProxyAction proxyAction;

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

            parent.setLayout(new GridLayout(1, false));

            tbm = new ToolBarManager(SWT.FLAT | SWT.RIGHT);

            final AddTabAction tabAction = new AddTabAction(this);
            tabAction.setImageDescriptor(ResourceUtils.getImageDescriptor(CoreConstants.PLUGIN_UI, CoreImages.TAB_OPEN));
            tbm.add(tabAction);

            final DuplicateTabAction tabDuplicate = new DuplicateTabAction(this);
            tabDuplicate.setImageDescriptor(ResourceUtils.getImageDescriptor(CoreConstants.PLUGIN_UI, CoreImages.TAB_DUPLICATE));
            tbm.add(tabDuplicate);

            final ParameterizeAction parameterizeAction = new ParameterizeAction(this);
            parameterizeAction.setImageDescriptor(ResourceUtils.getImageDescriptor(CoreConstants.PLUGIN_UI, CoreImages.PARAMETERIZE));
            tbm.add(parameterizeAction);

            final ImportMenuAction importAction = new ImportMenuAction(shell, this);
            importAction.setImageDescriptor(ResourceUtils.getImageDescriptor(CoreConstants.PLUGIN_UI, CoreImages.IMPORT));
            tbm.add(importAction);

            final ExportMenuAction exportMenuAction = new ExportMenuAction(shell, this);
            exportMenuAction.setImageDescriptor(ResourceUtils.getImageDescriptor(CoreConstants.PLUGIN_UI, CoreImages.EXPORT));
            tbm.add(exportMenuAction);

            proxyAction = new ProxyAction(this);
            proxyAction.setImageDescriptor(ResourceUtils.getImageDescriptor(CoreConstants.PLUGIN_UI, CoreImages.PROXY));
            tbm.add(proxyAction);

            final HelpDropDownAction helpAction = new HelpDropDownAction(shell);
            helpAction.setImageDescriptor(ResourceUtils.getImageDescriptor(CoreConstants.PLUGIN_UI, CoreImages.HELP));
            helpAction.setEnabled(true);
            tbm.add(helpAction);

            final Control toolBar = tbm.createControl(parent);
            toolBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

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
            final org.eclipse.swt.widgets.Label lbl = new org.eclipse.swt.widgets.Label(parent, SWT.WRAP);
            lbl.setText("HTTP4e2 failed to load: " + e.getClass().getSimpleName() + ": " + e.getMessage()
                    + (e.getCause() != null ? "\nCaused by: " + e.getCause() : ""));
            parent.layout(true);
        }
    }

    public void setFocus() {
        if (viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed()) {
            viewer.getControl().setFocus();
        }
    }

    @PreDestroy
    public void dispose() {
        if (tbm != null) {
            tbm.dispose();
            tbm = null;
        }
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
        if (proxyAction == null || tbm == null) {
            return;
        }
        proxyAction.setImageDescriptor(ResourceUtils.getImageDescriptor(CoreConstants.PLUGIN_UI,
                enabled ? CoreImages.PROXY_ENABLED : CoreImages.PROXY));
        tbm.update(true);
    }

    public Shell getShell() {
        return shell;
    }
}
