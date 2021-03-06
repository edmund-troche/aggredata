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

package com.ted.aggredata.server.dao;

import com.ted.aggredata.model.Group;
import com.ted.aggredata.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO for accessing the Group object
 */
public class GroupDAO extends AbstractDAO<Group> {


    static Logger logger = LoggerFactory.getLogger(GroupDAO.class);

    public static String DELETE_GROUP_QUERY = "delete from aggredata.group where id=?";
    public static String CREATE_GROUP_QUERY = "insert into aggredata.group (ownerUserId, description,custom1,custom2,custom3,custom4,custom5) values (?,?,?,?,?,?,?)";
    public static String COUNT_GROUP_QUERY = "select count(*) from aggredata.group where ownerUserId=? and description=?";
    public static String SAVE_GROUP_QUERY = "update aggredata.group set ownerUserId=?, description=?,custom1=?,custom2=?,custom3=?,custom4=?,custom5=? where id=?";
    public static String GET_GROUP_QUERY = "select g.id, g.ownerUserId, g.description, g.custom1,g.custom2,g.custom3,g.custom4,g.custom5, ? as role from aggredata.group g where g.description=? and g.ownerUserId=?";
    public static String GET_GROUPS_BY_USER_QUERY = "select g.id, g.ownerUserId, g.description, g.custom1,g.custom2,g.custom3,g.custom4,g.custom5, ug.role from aggredata.group g, aggredata.usergroup ug where ug.groupId = g.id and ug.userId=? and ug.role=?";
    public static String GET_GROUPS_WITH_GATEWAYS_BY_USER_QUERY = "select distinct g.id, g.ownerUserId, g.description, g.custom1,g.custom2,g.custom3,g.custom4,g.custom5, ug.role from aggredata.group g, aggredata.usergroup ug, aggredata.gatewaygroup gg where ug.groupId = g.id  and gg.groupId = ug.groupId and ug.userId=? order by ug.role, g.description";

    public static String GET_GROUP_BY_USER_QUERY = "select g.id, g.ownerUserId, g.description, g.custom1,g.custom2,g.custom3,g.custom4,g.custom5, ug.role from aggredata.group g, aggredata.usergroup ug where ug.groupId = g.id and ug.userId=? and g.id=?";
    public static String ADD_GROUP_MEMBERSHIP_QUERY = "insert into aggredata.usergroup(userId, groupId, role) values (?,?,?)";
    public static String ADD_GROUP_MEMBERSHIP_COUNT_QUERY = "select count(*) from aggredata.usergroup where userId=? and groupId=?";
    public static String UPDATE_GROUP_MEMBERSHIP_QUERY = "update aggredata.usergroup set role = ? where userId=? and groupId=?";
    public static String REMOVE_GROUP_MEMBERSHIP_QUERY = "delete from aggredata.usergroup where userId=? and groupId=?";
    public static String DELETE_GROUPS_FROM_MEMBERSHIP_QUERY = "delete from aggredata.usergroup where groupId=?";
    public static String REMOVE_GROUP_FROM_GATEWAYGROUP_QUERY = "delete from aggredata.gatewaygroup where groupId=?";


    public GroupDAO() {
        super("aggredata.group");
    }

    private RowMapper<Group> rowMapper = new RowMapper<Group>() {
        public Group mapRow(ResultSet rs, int rowNum) throws SQLException {
            Group group = new Group();
            group.setId(rs.getLong("id"));
            group.setOwnerUserId(rs.getLong("ownerUserId"));
            group.setRole(Group.Role.values()[rs.getInt("role")]);
            group.setDescription(rs.getString("description"));
            group.setCustom1(rs.getString("custom1"));
            group.setCustom2(rs.getString("custom2"));
            group.setCustom3(rs.getString("custom3"));
            group.setCustom4(rs.getString("custom4"));
            group.setCustom5(rs.getString("custom5"));
            return group;
        }
    };

    /**
     * Returns the group for the particular user but only if they are the owner.
     *
     * @param user
     * @param description
     * @return
     */
    public Group getOwnedGroup(User user, String description) {
        try {
            return getJdbcTemplate().queryForObject(GET_GROUP_QUERY, new Object[]{Group.Role.OWNER.ordinal(), description, user.getId()}, getRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            logger.debug("No Results returned");
            return null;
        }
    }


    /**
     * Creates a new group for the specified user.
     *
     * @param user
     * @param description
     * @return
     */
    public Group create(User user, String description) {
        if (getJdbcTemplate().queryForInt(COUNT_GROUP_QUERY, user.getId(), description) == 0) {
            //Create a new group object
            Group group = new Group();
            group.setDescription(description);
            group.setOwnerUserId(user.getId());
            group.setRole(Group.Role.OWNER);
            getJdbcTemplate().update(CREATE_GROUP_QUERY, group.getOwnerUserId(),
                    group.getDescription(),
                    group.getCustom1(),
                    group.getCustom2(),
                    group.getCustom3(),
                    group.getCustom4(),
                    group.getCustom5());

            //Add the user/group mapping to the database join table/
            Group newGroup = getOwnedGroup(user, group.getDescription());
            addGroupMembership(user, newGroup, Group.Role.OWNER);
            return newGroup;
        } else {
            return getOwnedGroup(user, description);
        }
    }

    public void save(Group group) {
        getJdbcTemplate().update(SAVE_GROUP_QUERY, group.getOwnerUserId(),
                group.getDescription(),
                group.getCustom1(),
                group.getCustom2(),
                group.getCustom3(),
                group.getCustom4(),
                group.getCustom5(),
                group.getId());
    }

    /**
     * Removes a group from the membership table
     *
     * @param user
     * @param group
     */
    public void removeGroupMembership(User user, Group group) {
        if (logger.isDebugEnabled()) logger.debug("removing " + user + " from " + group);
        getJdbcTemplate().update(REMOVE_GROUP_MEMBERSHIP_QUERY, user.getId(), group.getId());
    }


    /**
     * Removes a group from the membership table
     *
     * @param user
     * @param group
     */
    public void updateGroupMembership(User user, Group group, Group.Role role) {
        getJdbcTemplate().update(UPDATE_GROUP_MEMBERSHIP_QUERY, role.ordinal(), user.getId(), group.getId());
    }

    /**
     * updates a group
     *
     * @param user
     * @param group
     * @param role
     */
    public void addGroupMembership(User user, Group group, Group.Role role) {
        if (getJdbcTemplate().queryForInt(ADD_GROUP_MEMBERSHIP_COUNT_QUERY, user.getId(), group.getId()) == 0) {
            getJdbcTemplate().update(ADD_GROUP_MEMBERSHIP_QUERY, user.getId(), group.getId(), role.ordinal());
        } else {
            updateGroupMembership(user, group, role);
        }
    }

    /**
     * Returns a group list based on the user's role.
     *
     * @param user
     * @return
     */
    public List<Group> findGroupsByUser(User user, Group.Role role) {
        try {
            return getJdbcTemplate().query(GET_GROUPS_BY_USER_QUERY, new Object[]{user.getId(), role.ordinal()}, getRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            logger.debug("No Results returned");
            return null;
        }
    }

    /**
     * Returns a group list based on the user's role.
     *
     * @param user
     * @return
     */
    public List<Group> findGroupsWithGatewaysByUser(User user) {
        try {
            return getJdbcTemplate().query(GET_GROUPS_WITH_GATEWAYS_BY_USER_QUERY, new Object[]{user.getId()}, getRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            logger.debug("No Results returned");
            return null;
        }
    }

    /**
     * Returns the group for the specified user (used for Role checking)
     * @return
     */
    public Group findGroupByUser(User user, Long groupId) {
        try {
            return getJdbcTemplate().queryForObject(GET_GROUP_BY_USER_QUERY,new Object[]{user.getId(), groupId}, getRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            logger.debug("No Results returned");
            return null;
        }
    }


    public void delete(Group group) {
        logger.debug("Deleting group-user mappings");
        getJdbcTemplate().update(DELETE_GROUPS_FROM_MEMBERSHIP_QUERY, group.getId());
        logger.debug("Deleting gateway-group mappings");
        getJdbcTemplate().update(REMOVE_GROUP_FROM_GATEWAYGROUP_QUERY, group.getId());
        logger.debug("Deleting group");
        getJdbcTemplate().update(DELETE_GROUP_QUERY, new Object[]{group.getId()});
    }


    @Override
    public RowMapper<Group> getRowMapper() {
        return rowMapper;
    }
}
