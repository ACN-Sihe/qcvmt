package com.accenture.vmt;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.jdbc.support.ConnectionPreparer;

public class ClientIdentifierConnectionPreparer implements ConnectionPreparer {
    public Connection prepare(Connection conn) throws SQLException {
    	String clientIdentifier = System.getenv("QCVMT_CLIENT_IDENTIFIER_FOR_DB");
		if (StringUtils.isEmpty(clientIdentifier)) {
			clientIdentifier = "QCVMT";
		} else {
			clientIdentifier = clientIdentifier.replaceAll("[\"'\\\\/]", "");
		}
		CallableStatement cs = conn.prepareCall("{ call DBMS_SESSION.SET_IDENTIFIER('" + clientIdentifier + "') }");
        cs.execute();
        cs.close();
        return conn;
    }
}