package com.lifelover.dome.db.ext;

import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateColumnMapper implements ColumnMapper<Date> {
    @Override
    public Date map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        final String dateStr = r.getString(columnNumber);
        try{
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
