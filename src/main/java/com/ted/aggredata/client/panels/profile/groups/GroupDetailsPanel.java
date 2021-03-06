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

package com.ted.aggredata.client.panels.profile.groups;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.ted.aggredata.client.Aggredata;
import com.ted.aggredata.client.guiService.GWTGroupService;
import com.ted.aggredata.client.guiService.GWTGroupServiceAsync;
import com.ted.aggredata.client.guiService.TEDAsyncCallback;
import com.ted.aggredata.client.resources.lang.DashboardConstants;
import com.ted.aggredata.client.widgets.LongButton;
import com.ted.aggredata.model.Gateway;
import com.ted.aggredata.model.Group;
import com.ted.aggredata.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GroupDetailsPanel extends Composite {

    final GWTGroupServiceAsync groupService = (GWTGroupServiceAsync) GWT.create(GWTGroupService.class);

    static Logger logger = Logger.getLogger(GroupDetailsPanel.class.toString());



    interface MyUiBinder extends UiBinder<Widget, GroupDetailsPanel> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    @UiField
    TextBox descriptionField;
    @UiField
    Label descriptionFieldError;
    @UiField
    TextBox custom1Field;
    @UiField
    Label custom1FieldError;
    @UiField
    TextBox custom2Field;
    @UiField
    Label custom2FieldError;
    @UiField
    TextBox custom3Field;
    @UiField
    Label custom3FieldError;
    @UiField
    TextBox custom4Field;
    @UiField
    Label custom4FieldError;
    @UiField
    TextBox custom5Field;
    @UiField
    Label custom5FieldError;
    @UiField
    GroupGatewaysPanel groupGatewaysPanel;
    @UiField
    CaptionPanel captionPanel;

    @UiField HorizontalPanel custom1Panel;
    @UiField HorizontalPanel custom2Panel;
    @UiField HorizontalPanel custom3Panel;
    @UiField HorizontalPanel custom4Panel;
    @UiField HorizontalPanel custom5Panel;

    @UiField Label custom1Label;
    @UiField Label custom2Label;
    @UiField Label custom3Label;
    @UiField Label custom4Label;
    @UiField Label custom5Label;
    @UiField
    LongButton shareGroupButton;

    Group group;
    Integer groupHashCode = 0;
    List<Group> groupList = new ArrayList<Group>();
    List<Gateway> userGatewayList = new ArrayList<Gateway>();
    List<Gateway> groupGatewayList = new ArrayList<Gateway>();


    ChangeHandler saveChangeHanlder = new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent changeEvent) {
            if (group != null) doSave();
        }
    };

    public GroupDetailsPanel() {
        initWidget(uiBinder.createAndBindUi(this));

        //Hide the panels that are not being used for custom fields
        custom1Panel.setVisible(Aggredata.GLOBAL.getGroupCustomFields().getCustom1().trim().length() > 0);
        custom2Panel.setVisible(Aggredata.GLOBAL.getGroupCustomFields().getCustom2().trim().length() > 0);
        custom3Panel.setVisible(Aggredata.GLOBAL.getGroupCustomFields().getCustom3().trim().length() > 0);
        custom4Panel.setVisible(Aggredata.GLOBAL.getGroupCustomFields().getCustom4().trim().length() > 0);
        custom5Panel.setVisible(Aggredata.GLOBAL.getGroupCustomFields().getCustom5().trim().length() > 0);
        custom1Label.setText(Aggredata.GLOBAL.getGroupCustomFields().getCustom1());
        custom2Label.setText(Aggredata.GLOBAL.getGroupCustomFields().getCustom2());
        custom3Label.setText(Aggredata.GLOBAL.getGroupCustomFields().getCustom3());
        custom4Label.setText(Aggredata.GLOBAL.getGroupCustomFields().getCustom4());
        custom5Label.setText(Aggredata.GLOBAL.getGroupCustomFields().getCustom5());

        captionPanel.setCaptionHTML("<span style='color:white'>" + DashboardConstants.INSTANCE.groupDetails() + "</span>");

        descriptionField.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent keyUpEvent) {
                validate();
            }
        });

        descriptionField.addChangeHandler(saveChangeHanlder);
        custom1Field.addChangeHandler(saveChangeHanlder);
        custom2Field.addChangeHandler(saveChangeHanlder);
        custom3Field.addChangeHandler(saveChangeHanlder);
        custom4Field.addChangeHandler(saveChangeHanlder);
        custom5Field.addChangeHandler(saveChangeHanlder);

        shareGroupButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                //Show the popup that allows users to add/remove shared users from the group.
                GroupSharePopup groupSharePopup = new GroupSharePopup(group);
                groupSharePopup.addCloseHandler(new CloseHandler<PopupPanel>() {
                    @Override
                    public void onClose(CloseEvent<PopupPanel> popupPanelCloseEvent) {
                        logger.fine("Group Share Popup Closed.");
                    }
                });
                groupSharePopup.center();
                groupSharePopup.setPopupPosition(groupSharePopup.getAbsoluteLeft(), 100);
                groupSharePopup.show();
            }
        });

    }

    public void setGroupList(List<Group> groupList, List<Gateway> userGatewayList) {
        this.groupList = groupList;
        this.userGatewayList = userGatewayList;
    }


    /**
     * Sets all the fields of this panel to be enabled or disabled
     *
     * @param enabled
     */
    public void setEnabled(boolean enabled) {

        descriptionField.setEnabled(enabled);
        custom1Field.setEnabled(enabled);
        custom2Field.setEnabled(enabled);
        custom3Field.setEnabled(enabled);
        custom4Field.setEnabled(enabled);
        custom5Field.setEnabled(enabled);
        groupGatewaysPanel.setEnabled(enabled);

    }


    public boolean validate() {
        boolean valid = true;
        descriptionFieldError.setText("");

        if (group == null) return true;
        if (descriptionField.getText().trim().length() == 0) {
            valid = false;
            descriptionFieldError.setText("Required");
        }

        for (Group g : groupList) {
            if (!g.getId().equals(group.getId()) && g.getDescription().toLowerCase().equals(descriptionField.getText().trim().toLowerCase())) {
                valid = false;
                descriptionFieldError.setText("Already Used");
            }
        }

        return valid;
    }

    private void doSave() {
        if (validate()) {
            group.setDescription(descriptionField.getText().trim());
            group.setCustom1(custom1Field.getText().trim());
            group.setCustom2(custom2Field.getText().trim());
            group.setCustom3(custom3Field.getText().trim());
            group.setCustom4(custom4Field.getText().trim());
            group.setCustom5(custom5Field.getText().trim());
            if (group.hashCode() != groupHashCode) {
                logger.info("Group is dirty. Saving " + group);
                groupService.saveGroup(group, new TEDAsyncCallback<Group>() {
                    @Override
                    public void onSuccess(Group group) {
                        groupHashCode = group.hashCode();
                    }
                });
            }
        }
    }

    public void setGroup(User user, Group group, List<Gateway> groupGatewayList) {
        if (logger.isLoggable(Level.FINE)) logger.fine("Setting group " + group);
        setEnabled(group != null);
        descriptionField.setValue(group.getDescription());
        custom1Field.setValue(group.getCustom1());
        custom2Field.setValue(group.getCustom2());
        custom3Field.setValue(group.getCustom3());
        custom4Field.setValue(group.getCustom4());
        custom5Field.setValue(group.getCustom5());
        this.group = group;
        groupHashCode = group.hashCode();
        this.groupGatewayList = groupGatewayList;
        groupGatewaysPanel.setMap(user, group, userGatewayList, groupGatewayList);

        validate();
    }


    public void setUsers(List<User> users) {
        groupGatewaysPanel.setUsers(users);
    }

    public void addUserChangeHandler(ChangeHandler changeHandler){
        groupGatewaysPanel.addUserChangeHandler(changeHandler);
    }

    public User getSelectedUser() {
        return groupGatewaysPanel.getSelectedUser();
    }


}
