//package com.imst.event.map.cronjob.db.multitenant.provider;
//import java.util.Collection;
//
//import javax.sql.DataSource;
//
//import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
//import org.springframework.context.annotation.Conditional;
//import org.springframework.context.annotation.Configuration;
//
//import com.imst.event.map.cronjob.constants.MultitenantDatabaseE;
//import com.imst.event.map.cronjob.constants.Statics;
//import com.imst.event.map.cronjob.db.multitenant.cond.OnDatabaseOracleCondition;
//import com.imst.event.map.cronjob.vo.DataSourceInfo;
//
//@Configuration
//@Conditional(OnDatabaseOracleCondition.class)
//public class DataSourceBasedMultiTenantConnectionProviderOracleImpl extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {
//
//	private static final long serialVersionUID = 8866679902813519531L;
//	
//    public DataSourceBasedMultiTenantConnectionProviderOracleImpl() {
//    }
//
//    @Override
//    protected DataSource selectAnyDataSource() {
//    	
//    	Collection<DataSourceInfo> values = Statics.tenantDataSourceInfoMap.values();
//    	for (DataSourceInfo dataSourceInfo : values) {
//			if(dataSourceInfo.getMultitenantDatabaseE() == MultitenantDatabaseE.ORACLE) {
//				return dataSourceInfo.getDatasource();
//			}
//		}
//    	
//    	return null;
//    }
//
//    @Override
//    protected DataSource selectDataSource(String tenantIdentifier) {
//    	
//    	return Statics.tenantDataSourceInfoMap.get(tenantIdentifier).getDatasource();
//    }
//
//}