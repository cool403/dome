package com.lifelover.dome.db.ext;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateArgumentFactory extends AbstractArgumentFactory<Date> {
    /**
     * Constructs an {@link ArgumentFactory} for type {@code T}.
     *
     * @param sqlType the {@link Types} constant to use when the argument value is {@code null}.
     */
    public DateArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(Date value, ConfigRegistry config) {
        return (pos, stmt, ctx) -> {
            String formatted = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
            stmt.setString(pos, formatted);
        };
    }
}
