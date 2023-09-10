/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.gwt.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * A timer which sends an RPC call regularly to keep the session alive.<p>
 * 
 * @since 8.0.0
 */
public class CmsPingTimer {

    /**
     * The static instance.<p>
     */
    private static CmsPingTimer INSTANCE;

    /**
     * The interval for the RPC calls.<p>
     */
    public static final int PING_INTERVAL = 1000 * 60 * 5;

    /**
     * Starts the timer.<p>
     */
    public static void start() {

        if (INSTANCE == null) {
            INSTANCE = new CmsPingTimer();
            INSTANCE.run();
        }
    }

    /**
     * Installs the timer which fires the RPC calls.<p>
     */
    protected void run() {

        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

            public boolean execute() {

                CmsCoreProvider.getService().ping(new AsyncCallback<Void>() {

                    public void onFailure(Throwable caught) {

                        // do nothing

                    }

                    public void onSuccess(Void result) {

                        //do nothing
                    }
                });

                return true;
            }
        }, PING_INTERVAL);
    }
}
