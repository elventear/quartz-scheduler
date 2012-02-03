package org.quartz.impl.jdbcjobstore;

import java.sql.SQLException;
import java.util.HashMap;

import org.quartz.AbstractJobStoreTest;
import org.quartz.spi.JobStore;

public class JdbcJobStoreTest extends AbstractJobStoreTest {

	private HashMap<String, JobStoreSupport> stores = new HashMap<String, JobStoreSupport>();
	
    public void testNothing() {
        // nothing
    }

    @Override
    protected JobStore createJobStore(String name) {
        try {
            JdbcQuartzTestUtilities.createDatabase(name);
            JobStoreTX jdbcJobStore = new JobStoreTX();
            jdbcJobStore.setDataSource(name);
            jdbcJobStore.setTablePrefix("QRTZ_");
            jdbcJobStore.setInstanceId("SINGLE_NODE_TEST");
            jdbcJobStore.setInstanceName(name);
            jdbcJobStore.setUseDBLocks(true);

            stores.put(name, jdbcJobStore);
            
            return jdbcJobStore;
        } catch (SQLException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    protected void destroyJobStore(String name) {
        try {
        	JobStoreSupport jdbcJobStore = stores.remove(name);
        	jdbcJobStore.shutdown();
        	
            JdbcQuartzTestUtilities.destroyDatabase(name);
        } catch (SQLException e) {
            throw new AssertionError(e);
        }
    }
}
