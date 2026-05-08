/*
 *  Copyright 2017 Eclipse HttpClient (http4e) https://nextinterfaces.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.glindholm.plugin.http4e2.httpclient.ui.actions;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreImages;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.AuthItem;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ModelEvent;
import me.glindholm.plugin.http4e2.httpclient.core.util.BaseUtils;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;

/**
 * Per-tab authentication dialog. Reads and writes auth settings directly on the
 * tab's {@link ItemModel} rather than a shared global context object.
 */
public class AuthDialog extends TitleAreaDialog {

    private Button basicCheck;
    private Button digestCheck;
    private Button preemptiveCheck;
    private Text usernameBox;
    private Text passBox;
    private Text hostBox;
    private Text portBox;
    private Text realmBox;
    private Button okBtn;

    /** Working copy of auth settings; written back to the model on Apply. */
    private AuthItem authItem;

    private final ItemModel itemModel;

    public AuthDialog(final ViewPart view, final ItemModel itemModel) {
        super(view.getViewSite().getShell());
        setTitleImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.LOGO_DIALOG));
        this.itemModel = itemModel;
    }

    @Override
    protected Control createContents(final Composite parent) {
        final Control contents = super.createContents(parent);
        setTitle("Authentication");
        setMessage("BASIC and DIGEST Authentication.");
        populate();
        return contents;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE | SWT.BORDER);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return createControlGroup(composite);
    }

    private Composite createControlGroup(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        final GridLayout layout1 = new GridLayout();
        layout1.numColumns = 2;
        composite.setLayout(layout1);

        final Composite composite2 = new Composite(composite, SWT.NONE);
        final GridLayout layout2 = new GridLayout();
        layout2.numColumns = 1;
        composite2.setLayout(layout2);

        final Group authGroup = new Group(composite2, SWT.NONE);
        authGroup.setText("Auth Type");
        final GridLayout authLayout = new GridLayout();
        authLayout.numColumns = 1;
        authGroup.setLayout(authLayout);

        basicCheck = new Button(authGroup, SWT.CHECK);
        basicCheck.setText("BASIC");
        basicCheck.setSelection(false);
        basicCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                enableBoxes();
                okBtn.setEnabled(isEnabled());
                authItem.setBasic(basicCheck.getSelection());
            }
        });

        digestCheck = new Button(authGroup, SWT.CHECK);
        digestCheck.setText("DIGEST");
        digestCheck.setSelection(false);
        digestCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                enableBoxes();
                okBtn.setEnabled(isEnabled());
                authItem.setDigest(digestCheck.getSelection());
            }
        });
        final GridData authGroupData = new GridData(GridData.FILL_HORIZONTAL);
        authGroupData.widthHint = 160;
        authGroupData.heightHint = 70;
        authGroup.setLayoutData(authGroupData);

        final Group preGroup = new Group(composite2, SWT.NONE);
        preGroup.setText("Preemptive?");
        final GridLayout preLayout = new GridLayout();
        preLayout.numColumns = 2;
        preGroup.setLayout(preLayout);
        final GridData preGroupData = new GridData(GridData.FILL_HORIZONTAL);
        preGroupData.heightHint = 40;
        preGroup.setLayoutData(preGroupData);

        preemptiveCheck = new Button(preGroup, SWT.CHECK);
        preemptiveCheck.setText("Preemptive");
        preemptiveCheck.setSelection(true);
        preemptiveCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                authItem.setPreemtptive(preemptiveCheck.getSelection());
            }
        });

        createControlWidgets(composite);
        enableBoxes();
        return composite;
    }

    private void enableBoxes() {
        final boolean active = basicCheck.getSelection() || digestCheck.getSelection();
        usernameBox.setEnabled(active);
        passBox.setEnabled(active);
        hostBox.setEnabled(active);
        portBox.setEnabled(active);
        realmBox.setEnabled(active);
    }

    private void createControlWidgets(final Composite controlGroup) {
        final Group group = new Group(controlGroup, SWT.NONE);
        group.setText("Details");
        final GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 10;
        layout.marginWidth = 20;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        final GridData grData = new GridData(GridData.FILL_HORIZONTAL);
        grData.widthHint = 160;

        new Label(group, SWT.NONE).setText("Username *");
        usernameBox = new Text(group, SWT.BORDER);
        usernameBox.setLayoutData(grData);

        new Label(group, SWT.NONE).setText("Password *");
        passBox = new Text(group, SWT.PASSWORD | SWT.BORDER);
        passBox.setLayoutData(grData);

        usernameBox.addModifyListener(e -> {
            okBtn.setEnabled(isEnabled());
            authItem.setUsername(usernameBox.getText());
        });
        passBox.addModifyListener(e -> {
            okBtn.setEnabled(isEnabled());
            authItem.setPass(passBox.getText());
        });

        new Label(group, SWT.NONE).setText("Host");
        hostBox = new Text(group, SWT.BORDER);
        hostBox.setLayoutData(grData);
        hostBox.addModifyListener(e -> authItem.setHost(hostBox.getText()));

        new Label(group, SWT.NONE).setText("Port");
        portBox = new Text(group, SWT.BORDER);
        final GridData grData2 = new GridData(SWT.BEGINNING);
        grData2.widthHint = 30;
        portBox.setLayoutData(grData2);
        portBox.addModifyListener(e -> authItem.setPort(portBox.getText()));

        new Label(group, SWT.NONE).setText("Realm");
        realmBox = new Text(group, SWT.BORDER);
        realmBox.setLayoutData(grData);
        realmBox.addModifyListener(e -> authItem.setRealm(realmBox.getText()));
    }

    public void populate() {
        final AuthItem item = itemModel.getAuth();
        try {
            authItem = (item != null) ? item : new AuthItem();
            basicCheck.setSelection(authItem.isBasic());
            digestCheck.setSelection(authItem.isDigest());
            preemptiveCheck.setSelection(authItem.isPreemtptive());
            usernameBox.setText(authItem.getUsername());
            passBox.setText(authItem.getPass());
            hostBox.setText(authItem.getHost());
            portBox.setText(authItem.getPort());
            realmBox.setText(authItem.getRealm());

            if (isEnabled()) {
                okBtn.setEnabled(true);
                enableBoxes();
            }
        } catch (final SWTException e) {
            authItem = new AuthItem();
        }
    }

    private boolean isEnabled() {
        return (basicCheck.getSelection() || digestCheck.getSelection())
                && !BaseUtils.isEmpty(usernameBox.getText())
                && !BaseUtils.isEmpty(passBox.getText());
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {
        okBtn = createButton(parent, IDialogConstants.OK_ID, "Apply Authentication", true);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                itemModel.setAuth(authItem);
                itemModel.fireExecute(new ModelEvent(ModelEvent.AUTH, itemModel));
            }
        });
        okBtn.setEnabled(false);

        final Button removeBtn = createButton(parent, IDialogConstants.CANCEL_ID, "Don't Use Authentication", true);
        removeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                authItem.setBasic(false);
                authItem.setDigest(false);
                itemModel.setAuth(authItem);
                itemModel.fireExecute(new ModelEvent(ModelEvent.AUTH, itemModel));
            }
        });
    }
}
