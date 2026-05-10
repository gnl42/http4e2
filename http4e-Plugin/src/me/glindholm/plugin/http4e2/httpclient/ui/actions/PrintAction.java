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
package me.glindholm.plugin.http4e2.httpclient.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Shell;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreImages;
import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.FolderView;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;
import me.glindholm.plugin.http4e2.httpclient.core.util.shared.PrinterFacade;
import me.glindholm.plugin.http4e2.httpclient.ui.HdViewPart;

/**
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public class PrintAction extends Action {

    private final Shell shell;
    private final HdViewPart view;

    public PrintAction(final Shell shell, final HdViewPart view, final String title) {
        this.shell = shell;
        this.view = view;
        setText(title);
        setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.HELP)));
        setDescription("Print Http Packet");
        setToolTipText("Print Http Packet");
    }

    @Override
    public void run() {
        print(shell);
    }

    void print(final Shell sh) {
        final PrintDialog printDialog = new PrintDialog(sh, SWT.NONE);
        printDialog.setText("Print Http Packets");
        final PrinterData printerData = printDialog.open();
        if (!(printerData == null)) {
            final Printer printer = new Printer(printerData);
            final Thread printingThread = new Thread("Printing") {
                @Override
                public void run() {
                    try {
                        new PrinterFacade(getTextToPrint(), printer).print();
                    } catch (final Exception e) {
                        ExceptionHandler.handle(e);
                    } finally {
                        printer.dispose();
                    }
                }
            };
            printingThread.start();
        }
    }

    private String getTextToPrint() {
        final FolderView folderView = view.getFolderView();
        final ItemModel itemModel = folderView.getModel().getItemModel(folderView.getSelectionItemHash());
        final StringBuilder sb = new StringBuilder();
        sb.append("----------HTTP Request:---------\n");
        sb.append(itemModel.getRequest());
        sb.append("\n\n----------HTTP Response:--------\n");
        sb.append(itemModel.getResponse());
        return sb.toString();
    }

}