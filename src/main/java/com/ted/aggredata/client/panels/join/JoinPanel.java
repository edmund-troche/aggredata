/*
 * Copyright (c) 2012. The Energy Detective. All Rights Reserved
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ted.aggredata.client.panels.join;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.ted.aggredata.client.panels.login.LoginFormPanel;
import com.ted.aggredata.client.resources.DashboardImageResource;


public class JoinPanel extends Composite {

    @UiField
    AbsolutePanel contentPanel;

    interface MyUiBinder extends UiBinder<Widget, JoinPanel> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    public JoinPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        contentPanel.setSize("1024px", "480px");
        contentPanel.add(new Image(DashboardImageResource.INSTANCE.joinPanel()), 0, 25);
        contentPanel.add(new Image(DashboardImageResource.INSTANCE.tedLogo()), 40, 0);
        contentPanel.add(new Image(DashboardImageResource.INSTANCE.newUserTitle()), 360, 30);
        contentPanel.add(new JoinPanelForm(), 10, 80);
    }

}
