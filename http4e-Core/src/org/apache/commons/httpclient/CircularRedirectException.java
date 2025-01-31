/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/java/org/apache/commons/httpclient/CircularRedirectException.java,v 1.1 2005/01/14 19:40:39 olegk Exp $
 * $Revision: 155418 $
 * $Date: 2005-02-26 14:01:52 +0100 (Sat, 26 Feb 2005) $
 *
 * ====================================================================
 *
 *  Copyright 1999-2004 The Apache Software Foundation
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.commons.httpclient;

/**
 * Signals a circular redirect
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @since 3.0
 */
public class CircularRedirectException extends RedirectException {

    /**
     * Creates a new CircularRedirectException with a <tt>null</tt> detail message.
     */
    public CircularRedirectException() {
    }

    /**
     * Creates a new CircularRedirectException with the specified detail message.
     * 
     * @param message The exception detail message
     */
    public CircularRedirectException(final String message) {
        super(message);
    }

    /**
     * Creates a new CircularRedirectException with the specified detail message and cause.
     * 
     * @param message the exception detail message
     * @param cause   the <tt>Throwable</tt> that caused this exception, or <tt>null</tt> if the cause
     *                is unavailable, unknown, or not a <tt>Throwable</tt>
     */
    public CircularRedirectException(final String message, final Throwable cause) {
        super(message, cause);
    }

    private String redirectUrl;

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(final String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

}
