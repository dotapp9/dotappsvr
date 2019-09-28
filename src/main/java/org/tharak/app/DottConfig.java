package org.tharak.app;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tharak.dot.util.DataSourceMgr;

@Configuration
public class DottConfig {
	@Bean
	public DataSourceMgr getDataSourceMgr(DataSource ds) {
		DataSourceMgr mgr = DataSourceMgr.getMgr();
		mgr.setDataSource(ds);
		return mgr;
	}
}
