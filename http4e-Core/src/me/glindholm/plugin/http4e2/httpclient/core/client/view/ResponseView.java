/*
 *  Copyright 2017 Eclipse HttpClient (http4e) https://nextinterfaces.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.glindholm.plugin.http4e2.httpclient.core.client.view;

import org.eclipse.e4.ui.css.swt.CSSSWTConstants;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import me.glindholm.plugin.http4e2.editor.xml.XMLConfiguration;
import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreImages;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ModelEvent;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.DocumentUtils;
import me.glindholm.plugin.http4e2.httpclient.core.misc.ColorManagerAdaptor;
import me.glindholm.plugin.http4e2.httpclient.core.misc.Styles;
import me.glindholm.plugin.http4e2.httpclient.core.util.JunkUtils;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;

/**
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
class ResponseView {

    private final String[] text = { "" };

    private final static int ITEM_RAW = 0;
    private final static int ITEM_PRETTY = 1;
    private final static int ITEM_JSON = 2;
    private final static int ITEM_HEX = 3;
    private final static int ITEM_BRW = 4;
    private final static int ITEM_CERT = 5;
    private int currItem = ITEM_RAW;

    private final StyledText responseText;
    private final StyledText jsonText;
    private final StyledText certText;
    private final Browser browser;
    private final PayloadMenu payloadMenu;
    private final PayloadMenu payloadMenuJson;
    private ToolItem certToolItem;

    ResponseView(final ItemModel model, final Composite parent) {

        final ViewForm vForm = doToolbar(CoreConstants.TITLE_RESPONSE, model, parent);
        responseText = buildEditorText(vForm);
        jsonText = buildJsonEditorText(vForm);
        certText = buildCertEditorText(vForm);
        vForm.setContent(responseText);

        browser = new Browser(vForm, SWT.NONE);

        // Use system editor colours as defaults; e4 CSS (.http4e-viewer) overrides them.
        final Display display = Display.getCurrent();
        final Color defaultFg = display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
        final Color defaultBg = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);

        responseText.setData(CSSSWTConstants.CSS_CLASS_NAME_KEY, "http4e-viewer");
        responseText.setFont(ResourceUtils.getFont(Styles.getInstance(parent.getShell()).getFontMonospaced()));
        responseText.setForeground(defaultFg);
        responseText.setBackground(defaultBg);
        responseText.setEditable(false);
        responseText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(final MouseEvent e) {
                model.fireExecute(new ModelEvent(ModelEvent.RESPONSE_RESIZED, model));
            }
        });
        responseText.addKeyListener(new ExecuteKeyListener(() -> model.fireExecute(new ModelEvent(ModelEvent.REQUEST_START, model))));

        jsonText.setData(CSSSWTConstants.CSS_CLASS_NAME_KEY, "http4e-viewer");
        jsonText.setFont(ResourceUtils.getFont(Styles.getInstance(parent.getShell()).getFontMonospaced()));
        jsonText.setForeground(defaultFg);
        jsonText.setBackground(defaultBg);
        jsonText.setEditable(false);
        jsonText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(final MouseEvent e) {
                model.fireExecute(new ModelEvent(ModelEvent.RESPONSE_RESIZED, model));
            }
        });
        jsonText.addKeyListener(new ExecuteKeyListener(() -> model.fireExecute(new ModelEvent(ModelEvent.REQUEST_START, model))));

        certText.setData(CSSSWTConstants.CSS_CLASS_NAME_KEY, "http4e-viewer");
        certText.setFont(ResourceUtils.getFont(Styles.getInstance(parent.getShell()).getFontMonospaced()));
        certText.setForeground(defaultFg);
        certText.setBackground(defaultBg);
        certText.setEditable(false);

        final Menu popupMenu = new Menu(responseText);
        payloadMenu = new PayloadMenu(responseText, popupMenu);
        responseText.setMenu(popupMenu);

        final Menu popupMenu2 = new Menu(jsonText);
        payloadMenuJson = new PayloadMenu(jsonText, popupMenu2);
        jsonText.setMenu(popupMenu2);

        final ToolItem i_raw = Utils.getItem(ITEM_RAW, vForm);
        final ToolItem i_pretty = Utils.getItem(ITEM_PRETTY, vForm);
        final ToolItem i_hex = Utils.getItem(ITEM_HEX, vForm);
        final ToolItem i_brw = Utils.getItem(ITEM_BRW, vForm);
        final ToolItem i_json = Utils.getItem(ITEM_JSON, vForm);
        final ToolItem i_cert = Utils.getItem(ITEM_CERT, vForm);
        certToolItem = i_cert;
        i_raw.setSelection(true);

        i_raw.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                responseText.setText(text[0]);
                if (currItem != ITEM_RAW && currItem != ITEM_PRETTY && currItem != ITEM_HEX) {
                    vForm.setContent(responseText);
                    vForm.redraw();
                }
                currItem = ITEM_RAW;
            }
        });

        i_pretty.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                responseText.setText(JunkUtils.prettyText(text[0]));
                if (currItem != ITEM_RAW && currItem != ITEM_PRETTY && currItem != ITEM_HEX) {
                    vForm.setContent(responseText);
                    vForm.redraw();
                }
                currItem = ITEM_PRETTY;
            }
        });

        i_hex.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                JunkUtils.hexText(responseText, text[0]);
                if (currItem != ITEM_RAW && currItem != ITEM_PRETTY && currItem != ITEM_HEX) {
                    vForm.setContent(responseText);
                    vForm.redraw();
                }
                currItem = ITEM_HEX;
            }
        });

        i_brw.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                browser.setText(JunkUtils.prettyText(text[0]));
                if (currItem != ITEM_BRW) {
                    vForm.setContent(browser);
                    vForm.redraw();
                }
                currItem = ITEM_BRW;
            }
        });

        i_json.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                jsonText.setText(JunkUtils.jsonText(JunkUtils.prettyText(text[0]), false));
                if (currItem != ITEM_JSON) {
                    vForm.setContent(jsonText);
                    vForm.redraw();
                }
                currItem = ITEM_JSON;
            }
        });

        i_cert.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (currItem != ITEM_CERT) {
                    vForm.setContent(certText);
                    vForm.redraw();
                }
                currItem = ITEM_CERT;
            }
        });
    }

    private StyledText buildEditorText(final Composite parent) {
        final SourceViewer sourceViewer = new SourceViewer(parent, null, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);

        final XMLConfiguration sourceConf = new XMLConfiguration(new ColorManagerAdaptor(ResourceUtils.getResourceCache()));
        sourceViewer.configure(sourceConf);
        sourceViewer.setDocument(DocumentUtils.createDocument2());

        return sourceViewer.getTextWidget();
    }

    private StyledText buildJsonEditorText(final Composite parent) {
        final SourceViewer sourceViewer = new SourceViewer(parent, null, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
        final StyledText st = sourceViewer.getTextWidget();
        final JSONLineStyler jsonStyler = new JSONLineStyler();
        st.addLineStyleListener(jsonStyler);
        return st;
    }

    private StyledText buildCertEditorText(final Composite parent) {
        final StyledText st = new StyledText(parent, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY);
        return st;
    }

    private ViewForm doToolbar(final String title, final ItemModel model, final Composite parent) {

        final ViewForm vForm = ViewUtils.buildViewForm(title, model, parent);
        final ToolBar bar = new ToolBar(vForm, SWT.FLAT);

        final ToolItem i_raw = new ToolItem(bar, SWT.RADIO);
        i_raw.setImage(ResourceUtils.getImage(CoreConstants.PLUGIN_CORE, CoreImages.RAW));
        i_raw.setToolTipText("Raw View");

        final ToolItem i_pretty = new ToolItem(bar, SWT.RADIO);
        i_pretty.setImage(ResourceUtils.getImage(CoreConstants.PLUGIN_CORE, CoreImages.PRETTY));
        i_pretty.setToolTipText("Pretty View");

        final ToolItem i_json = new ToolItem(bar, SWT.RADIO);
        i_json.setImage(ResourceUtils.getImage(CoreConstants.PLUGIN_CORE, CoreImages.JSON));
        i_json.setToolTipText("JSON View");

        final ToolItem i_hex = new ToolItem(bar, SWT.RADIO);
        i_hex.setImage(ResourceUtils.getImage(CoreConstants.PLUGIN_CORE, CoreImages.HEX));
        i_hex.setToolTipText("Hex View");

        final ToolItem i_br = new ToolItem(bar, SWT.RADIO);
        i_br.setImage(ResourceUtils.getImage(CoreConstants.PLUGIN_CORE, CoreImages.BROWSER));
        i_br.setToolTipText("View in Browser");

        final ToolItem i_cert = new ToolItem(bar, SWT.RADIO);
        i_cert.setImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.AUTH_TAB));
        i_cert.setToolTipText("Certificate");

        vForm.setTopCenter(bar);

        return vForm;
    }

    public void setHttpText(final String txt) {
        text[0] = txt;
        if (currItem == ITEM_CERT) {
            return; // keep cert view as-is
        }
        if (currItem == ITEM_RAW) {
            responseText.setText(text[0]);

        } else if (currItem == ITEM_PRETTY) {
            responseText.setText(JunkUtils.prettyText(text[0]));

        } else if (currItem == ITEM_HEX) {
            JunkUtils.hexText(responseText, text[0]);

        } else if (currItem == ITEM_BRW) {
            browser.setText(JunkUtils.prettyText(text[0]));

        } else {
            jsonText.setText(JunkUtils.jsonText(JunkUtils.prettyText(text[0]), false));
        }
    }

    public void setCertificateInfo(final String certInfo, final boolean selfSigned) {
        final String info = certInfo == null || certInfo.isBlank() ? "(No certificate information available - HTTP connection or not captured)" : certInfo;
        certText.setText(info);
        if (certToolItem != null && !certToolItem.isDisposed()) {
            final String iconKey = selfSigned ? CoreImages.CERT_WARN : (certInfo != null && !certInfo.isBlank() ? CoreImages.CERT_OK : CoreImages.AUTH_TAB);
            certToolItem.setImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, iconKey));
            certToolItem.setToolTipText(selfSigned ? "Certificate (Self-Signed ⚠)" : "Certificate");
        }
    }

    public void setPayloadFilename(final String filename) {
        payloadMenu.setFilename(filename);
        payloadMenuJson.setFilename(filename);
    }

}
