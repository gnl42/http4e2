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

import java.util.List;

import org.eclipse.e4.ui.css.swt.CSSSWTConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreContext;
import me.glindholm.plugin.http4e2.httpclient.core.CoreObjects;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.FolderModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ModelEvent;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ModelListener;
import me.glindholm.plugin.http4e2.httpclient.core.misc.ResourceCache;
import me.glindholm.plugin.http4e2.httpclient.core.util.BaseUtils;

public class FolderView implements CoreConstants, ModelListener {

    private CTabFolder folder;
    private final FolderModel model;

    public FolderView(final Composite parent, final FolderModel folderModel) {
        model = folderModel;
        CoreContext.getContext().putObject(CoreObjects.RESOURCE_CACHE, new ResourceCache(/* parent */));
        model.addListener(this);
        buildFolder(parent);

        final byte[] data = BaseUtils.readFromPrefs(FolderModel.PREF_VIEW_NAME);
        final List<ItemModel> imList = model.deserialize(data);
        final int size = imList.size();
        for (int i = size - 1; i > -1; i--) {
            buildTab(new CTabItem(folder, SWT.NONE), imList.get(i));
        }
    }

    public void enableAuth() {
        model.doAuth();
    }

    public void enableProxy() {
        model.doProxy();
    }

    private void buildFolder(final Composite parent) {

        // IDocument document;
        folder = new CTabFolder(parent, SWT.MULTI | SWT.CLOSE | SWT.BORDER);
        folder.setTabPosition(SWT.DOWN);
        folder.setTabHeight(20);
        folder.setBackgroundMode(SWT.INHERIT_DEFAULT);
        folder.setBorderVisible(false);
        // Apply e4 CSS class – colours managed by the stylesheet
        folder.setData(CSSSWTConstants.CSS_CLASS_NAME_KEY, "http4e-tab-folder");

        folder.addCTabFolder2Listener(new CTabFolder2Adapter() {

            @Override
            public void close(final CTabFolderEvent e) {
                if (model.getItemCount() > 1) {
                    final CTabItem tabItem = (CTabItem) e.item;
                    model.removeItem(tabItem.hashCode());
                } else {
                    e.doit = false;
                }
            }
        });

        folder.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDoubleClick(final MouseEvent e) {
                addTab();
            }
        });
    }

    public int getSelectionItemHash() {
        return folder.getSelection().hashCode();
    }

    public Control getControl() {
        return folder;
    }

    public void removeTab() {
        folder.getSelection().dispose();
    }

    public void duplicateTab() {
        final CTabItem tab = new CTabItem(folder, SWT.NONE);
        final ItemModel im0 = model.getItemModel(folder.getSelection().hashCode());
        buildTab(tab, im0.clone());
    }

    public void addTab() {
        final CTabItem tab = new CTabItem(folder, SWT.NONE);
        final ItemModel iModel = new ItemModel(model);
        buildTab(tab, iModel);
    }

    public void buildTab(final ItemModel iModel) {
        buildTab(new CTabItem(folder, SWT.NONE), iModel);
    }

    public void buildTab(final CTabItem tab, final ItemModel iModel) {
        iModel.init(tab.hashCode());
        final ItemView iView = new ItemView(tab, iModel);
        Utils.modelToView(iModel, iView);
        folder.setSelection(tab);
        model.putItem(iModel);
        iModel.fireExecute(new ModelEvent(ModelEvent.HTTP_METHOD_CHANGE, iModel));
        iModel.fireExecute(new ModelEvent(ModelEvent.PARAMS_FOCUS_LOST, iModel));
        iView.urlCombo.setFocus();
        iModel.fireExecute(new ModelEvent(ModelEvent.AUTH, CoreConstants.NULL_MODEL));
        iModel.fireExecute(new ModelEvent(ModelEvent.PROXY, CoreConstants.NULL_MODEL));
        // iView.urlCombo.setItems(model.getUrlHistory());
        // iModel.fireExecute( new ModelEvent(ModelEvent.CONTENT_TYPE_CHANGE,
        // iModel));
    }

    @Override
    public void executed(final ModelEvent e) {
        if (e.getType() == ModelEvent.ITEM_ADD) {
            final CTabItem tab = new CTabItem(folder, SWT.NONE);
            final ItemModel iModel = (ItemModel) e.getModel();
            buildTab(tab, iModel);

        } else if (e.getType() == ModelEvent.FOLDER_FOCUS_GAINED) {
            // CSS handles active-tab colours; just mark the widget as focused
            folder.setData(CSSSWTConstants.CSS_CLASS_NAME_KEY, "http4e-tab-folder");
            folder.redraw();

        } else if (e.getType() == ModelEvent.FOLDER_FOCUS_LOST) {
            folder.setData(CSSSWTConstants.CSS_CLASS_NAME_KEY, "http4e-tab-folder");
            folder.redraw();

        } else if (e.getType() == ModelEvent.PARAMETERIZE_CHANGE) {
            for (final ItemModel m : model.getItemModels()) {
                m.fireExecute(new ModelEvent(ModelEvent.PARAMS_FOCUS_LOST, CoreConstants.NULL_MODEL));
            }
        }
    }

    public void setFocus(final boolean gained) {
        if (gained) {
            model.fireExecute(new ModelEvent(ModelEvent.FOLDER_FOCUS_GAINED, CoreConstants.NULL_MODEL));
        } else {
            model.fireExecute(new ModelEvent(ModelEvent.FOLDER_FOCUS_LOST, CoreConstants.NULL_MODEL));
        }
    }

    public FolderModel getModel() {
        return model;
    }

    public void focus() {
        folder.setFocus();
    }

    public void doDispose() {
        final ResourceCache imageCache = (ResourceCache) CoreContext.getContext().getObject(CoreObjects.RESOURCE_CACHE);
        if (imageCache != null) {
            imageCache.dispose();
        }
        model.doDispose();
        folder.dispose();
    }

    public int getSelectionIndex() {
        return folder.getSelectionIndex();
    }
    // // ================ MAIN ======================
    // public static void main( String[] args){
    // String ROOT_CORE = "C:/.work/http4e/me.glindholm.plugin.http4e2/Core";
    // String ROOT_UI = "C:/.work/http4e/me.glindholm.plugin.http4e2/Plugin";
    //
    // CoreContext.getContext().putObject(CoreObjects.ROOT_PATH_CORE, ROOT_CORE);
    // CoreContext.getContext().putObject(CoreObjects.ROOT_PATH_UI, ROOT_UI);
    //
    // CoreContext.getContext().putObject(CoreConstants.IS_STANDALONE, "nonull");
    //
    // Display display = new Display();
    // Shell shell = new Shell(display);
    // final ResourceCache imageCache = new ResourceCache();
    // // shell.setImage(imageCache.getImage(CoreImages.LOGO));
    // shell.setLayout(new FillLayout());
    // shell.setBounds(425, 625, 825, 350);
    //
    // final FolderView folderView = new FolderView(shell, new FolderModel());
    // shell.addDisposeListener(new DisposeListener() {
    // public void widgetDisposed( DisposeEvent e){
    // imageCache.dispose();
    // folderView.doDispose();
    // }
    // });
    // // TODO add similar listener to plugin view
    // shell.addShellListener(new ShellAdapter() {
    // public void shellActivated( ShellEvent e){
    // folderView.setFocus(true);
    // }
    //
    // public void shellDeactivated( ShellEvent e){
    // folderView.setFocus(false);
    // }
    // });
    //
    // shell.open();
    // while (!shell.isDisposed()) {
    // if (!display.readAndDispatch())
    // display.sleep();
    // }
    // display.dispose();
    // }
}
