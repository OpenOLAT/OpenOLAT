package org.olat.core.commons.persistence;

import org.olat.core.configuration.Initializable;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.sql.Statement;

public class WaitForTheDatabaseDriverManagerDataSource extends DriverManagerDataSource implements Initializable {

	private static final Logger LOGGER = Tracing.createLoggerFor(
			WaitForTheDatabaseDriverManagerDataSource.class);

	@Override
	public void init() {
		Statement statement = null;

		while(true) {
			try {
				LOGGER.info(Tracing.M_AUDIT, "Waiting for the database: " + getUrl());
				statement = getConnection().createStatement();
				break;
			} catch (SQLException e) {
				LOGGER.warn(e.getMessage(), e);
			} finally {
				try {
					if (statement != null) {
						statement.close();
					}
				} catch (SQLException e2) {
					throw new StartupException("Could not close the SQL statement.", e2);
				}
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new StartupException("Waiting loop was interrupted.", e);
			}
		}
	}
}
