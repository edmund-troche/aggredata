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

import com.ted.aggredata.model.Gateway;
import com.ted.aggredata.model.MTU;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO for accessing the MTU Object
 */
public class MTUDAO extends AbstractDAO<MTU> {

    @Autowired
    EnergyDataDAO energyDataDAO;


    public static String DELETE_MTU_QUERY = "delete from aggredata.mtu where id=? and gatewayId=?";
    public static String CREATE_MTU_QUERY = "insert into aggredata.mtu (id, gatewayId,  type, description) values (?,?,?,?)";
    public static String SAVE_MTU_QUERY = "update aggredata.mtu set gatewayId=?, type=?, description=? where id=? and gatewayId=?";
    public static String GET_BY_GATEWAY_QUERY = "select id, gatewayId,  type, description from aggredata.mtu where gatewayId=?";
    public static String FIND_BY_ID_QUERY = "select id, gatewayId,  type, description from aggredata.mtu where gatewayId=? and id=?";

    public MTUDAO() {
        super("aggredata.mtu");
    }

    private RowMapper<MTU> rowMapper = new RowMapper<MTU>() {
        public MTU mapRow(ResultSet rs, int rowNum) throws SQLException {
            MTU mtu = new MTU();
            mtu.setId(rs.getLong("id"));
            mtu.setGatewayId(rs.getLong("gatewayId"));
            mtu.setType(MTU.MTUType.values()[rs.getInt("type")]);
            mtu.setDescription(rs.getString("description"));
            return mtu;
        }
    };

    public MTU create(Gateway gateway, MTU newMTU) {
        MTU mtu = findById(gateway.getId(), newMTU.getId());
        if (mtu == null) {
            if (logger.isDebugEnabled()) logger.debug("creating new mtu " + newMTU);
            getJdbcTemplate().update(CREATE_MTU_QUERY, newMTU.getId(), gateway.getId(), newMTU.getType().ordinal(), newMTU.getDescription());
            return findById(gateway.getId(), newMTU.getId());
        }
        return mtu;
    }


    public void save(MTU mtu) {
        if (logger.isDebugEnabled()) logger.debug("saving  mtu " + mtu);
        getJdbcTemplate().update(SAVE_MTU_QUERY, mtu.getGatewayId(), mtu.getType().ordinal(), mtu.getDescription(), mtu.getId(), mtu.getGatewayId());
    }


    /**
     * Returns the MTU's for the given gateway
     *
     * @param gateway
     * @return
     */
    public List<MTU> findByGateway(Gateway gateway) {
        try {
            return getJdbcTemplate().query(GET_BY_GATEWAY_QUERY, new Object[]{gateway.getId()}, getRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            logger.debug("No Results returned");
            return null;
        }
    }


    @Override
    public RowMapper<MTU> getRowMapper() {
        return rowMapper;
    }

    public void delete(Gateway gateway, MTU mtu) {
        energyDataDAO.deleteEnergyData(gateway, mtu);
        if (logger.isDebugEnabled()) logger.debug("removing " + mtu + " from mtu table");
        getJdbcTemplate().update(DELETE_MTU_QUERY, mtu.getId(), gateway.getId());
    }


    @Override
    public MTU findById(Long id) {
        logger.error("MTU findById not implemented");
        throw new NotImplementedException();
    }


    public MTU findById(Long gatewayId, Long mtuId) {
        try {
            return getJdbcTemplate().queryForObject(FIND_BY_ID_QUERY, new Object[]{gatewayId, mtuId}, getRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            logger.debug("No Results returned");
            return null;
        }
    }
}
