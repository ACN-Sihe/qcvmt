package com.mtl.qcvmt.n4;

import com.mtl.qcvmt.exception.N4ConnectionException;
import com.mtl.qcvmt.exception.N4QueryException;
import java.sql.SQLTransientConnectionException;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class N4QueryRepository {

  private final JdbcTemplate n4JdbcTemplate;

  public N4QueryRepository(@Qualifier("n4JdbcTemplate") JdbcTemplate n4JdbcTemplate) {
    this.n4JdbcTemplate = n4JdbcTemplate;
  }

  public List<Map<String, Object>> queryForList(String sql, Object... args) {
    try {
      return n4JdbcTemplate.queryForList(sql, args);
    } catch (DataAccessException ex) {
      throw translateException("Failed to execute N4 list query", ex);
    }
  }

  public Map<String, Object> queryForMap(String sql, Object... args) {
    try {
      return n4JdbcTemplate.queryForMap(sql, args);
    } catch (DataAccessException ex) {
      throw translateException("Failed to execute N4 map query", ex);
    }
  }

  public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) {
    try {
      return n4JdbcTemplate.queryForObject(sql, requiredType, args);
    } catch (DataAccessException ex) {
      throw translateException("Failed to execute N4 object query", ex);
    }
  }

  private RuntimeException translateException(String message, DataAccessException ex) {
    Throwable cause = ex.getCause();
    String lowerMessage = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
    if (cause instanceof SQLTransientConnectionException
        || lowerMessage.contains("connection refused")
        || lowerMessage.contains("connection timed out")) {
      return new N4ConnectionException(message, ex);
    }
    return new N4QueryException(message, ex);
  }
}
