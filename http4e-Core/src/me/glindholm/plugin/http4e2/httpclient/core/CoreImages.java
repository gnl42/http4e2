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
package me.glindholm.plugin.http4e2.httpclient.core;

import me.glindholm.plugin.http4e2.httpclient.core.util.BaseUtils;

/**
 * This class contains all images relative path.
 *
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public interface CoreImages {

    String FOLDER = BaseUtils.noNull((String) CoreContext.getContext().getObject(CoreObjects.ROOT_PATH_CORE));
    String FOLDER_PLUGIN = BaseUtils.noNull((String) CoreContext.getContext().getObject(CoreObjects.ROOT_PATH_UI));

    String icons = FOLDER + "/icons/";

    String LOGO = icons + "logo.png";
    String ATTACH = icons + "attach.png";
    String MENU = icons + "menu.png";
    String RESIZE = icons + "resize.png";
    String GO = icons + "go.png";
    String TITLE_LINE = icons + "title-line.png";
    String BROWSER = icons + "browser.png";
    String LOADING_OFF = icons + "loading-small.png";
    String LOADING_ON = icons + "loading-small.gif"; // must remain GIF — SWT ImageLoader requires animated GIF for multi-frame animation
    String BLANK16 = icons + "blank16.png";
    String BLANK20 = icons + "blank20.png";
    String BLANK24 = icons + "blank24.png";
    String PROGRESS_STOP_ON = icons + "progress_stop_on.png";
    String PROGRESS_STOP_OFF = icons + "progress_stop_off.png";
    String CLOSE_ROW = icons + "close-row.png";
    String JSON = icons + "json.png";
    String DELETE = icons + "delete.png";

    String HTML_TEXT = icons + "html-text.png";
    String HTML_BUTTON = icons + "html-button.png";
    String HTML_SUBMIT = icons + "html-button.png";
    String HTML_SELECT = icons + "html-select.png";
    String HTML_CHECK = icons + "html-check.png";
    String HTML_RADIO = icons + "html-radio.png";
    String HTML_BROWSE = icons + "html-browse.png";
    String HTML_HIDDEN = icons + "html-hidden.png";

    String RAW = icons + "raw.png";
    String PRETTY = icons + "pretty.png";
    String HEX = icons + "hex.png";
    String FILE_OPEN = icons + "file-open.png";
    String FILE_OPEN_DIS = icons + "file-open-dis.png";

    String SYNCED = icons + "synced.png";
    String STEP_PREV = icons + "step-prev.png";

    String ASSIST_HEADER = icons + "assist-header.png";
    String ASSIST_HEADER_CACHED = icons + "assist-param.png";
    String ASSIST_TEMPLATE = icons + "assist-template.png";

    String FILE_HEADERS_ROOT = FOLDER + "/resources/";
    String FILE_HEADERS = FOLDER + "/resources/headers.properties";
    String FILE_HEADERS_VALUES = FOLDER + "/resources/headers-values.properties";

    String PARAMETERIZE = FOLDER_PLUGIN + "/icons/" + "params.png";
    String TAB_DUPLICATE = FOLDER_PLUGIN + "/icons/" + "duplicate.png";
    String TAB_OPEN = FOLDER_PLUGIN + "/icons/" + "tab-new.png";
    String IMPORT = FOLDER_PLUGIN + "/icons/" + "import.png";
    String EXPORT = FOLDER_PLUGIN + "/icons/" + "export.png";
    String JS = FOLDER_PLUGIN + "/icons/" + "js.png";
    String CSHARP = FOLDER_PLUGIN + "/icons/" + "csharp.png";
    String JAVA = FOLDER_PLUGIN + "/icons/" + "java.png";
    String FLEX = FOLDER_PLUGIN + "/icons/" + "flex.png";
    String RUBY = FOLDER_PLUGIN + "/icons/" + "ruby.png";
    String JMETER = FOLDER_PLUGIN + "/icons/" + "jmeter.png";
    String VB = FOLDER_PLUGIN + "/icons/" + "vb.png";
    String PHP = FOLDER_PLUGIN + "/icons/" + "php.png";
    String OC = FOLDER_PLUGIN + "/icons/" + "oc.png";
    String PYTHON = FOLDER_PLUGIN + "/icons/" + "python.png";
    String HELP = FOLDER_PLUGIN + "/icons/" + "help.png";
    String PRINT = FOLDER_PLUGIN + "/icons/" + "print_edit.png";
    String TOOLS = FOLDER_PLUGIN + "/icons/" + "tools.png";
    String LOGO_DIALOG = FOLDER_PLUGIN + "/icons/" + "logo-dialog.png";
    String PAYPAL = FOLDER_PLUGIN + "/icons/" + "paypal.png";
    String MAGN = FOLDER_PLUGIN + "/icons/" + "magn.png";
    String AUTH = FOLDER_PLUGIN + "/icons/" + "auth.png";
    String AUTH_ENABLED = FOLDER_PLUGIN + "/icons/" + "auth_enabled.png";
    String AUTH_TAB = FOLDER_PLUGIN + "/icons/" + "auth_tab.png";
    String PROXY = FOLDER_PLUGIN + "/icons/" + "proxy.png";
    String PROXY_ENABLED = FOLDER_PLUGIN + "/icons/" + "proxy_enabled.png";
    String PROXY_TAB = FOLDER_PLUGIN + "/icons/" + "proxy_tab.png";
    String CERT_OK = FOLDER_PLUGIN + "/icons/" + "cert_ok.png";
    String CERT_WARN = FOLDER_PLUGIN + "/icons/" + "cert_warn.png";

}
