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

import me.glindholm.plugin.http4e2.httpclient.core.client.view.FolderView;
import me.glindholm.plugin.http4e2.httpclient.ui.HdViewPart;

/**
 * Legacy action delegate — no longer used as a 3.x IViewActionDelegate.
 * Kept for backwards-compatibility reference; toolbar actions now go through
 * the {@link AddTabAction} / {@link DuplicateTabAction} etc. directly.
 *
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public class HdActionDelegate {

    public HdViewPart hdPart = null;

    public void init(final HdViewPart viewPart) {
        hdPart = viewPart;
    }

    public void run(final String id) {
        final FolderView folderView = hdPart.getFolderView();
        if (id.equals("add.tab")) {
            folderView.addTab();
        } else if (id.equals("remove.tab")) {
            folderView.removeTab();
        }
    }

}