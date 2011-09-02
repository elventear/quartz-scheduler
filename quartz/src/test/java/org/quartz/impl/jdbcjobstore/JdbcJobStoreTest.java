package org.quartz.impl.jdbcjobstore;

//import java.sql.SQLException;
//
//import org.quartz.AbstractJobStoreTest;
//import org.quartz.spi.JobStore;

public class JdbcJobStoreTest /*extends AbstractJobStoreTest*/ {

    public void testNothing() {
        // nothing
    }

//    @Override
//    protected JobStore createJobStore(String name) {
//        try {
//            JdbcQuartzTestUtilities.createDatabase(name);
//            JobStoreTX jdbcJobStore = new JobStoreTX();
//            jdbcJobStore.setDataSource(name);
//            jdbcJobStore.setTablePrefix("QRTZ_");
//            jdbcJobStore.setInstanceId(name);
//            jdbcJobStore.setUseDBLocks(true);
//            return jdbcJobStore;
//        } catch (SQLException e) {
//            throw new AssertionError(e);
//        }
//    }
//
//    @Override
//    protected void destroyJobStore(String name) {
//        try {
//            JdbcQuartzTestUtilities.destroyDatabase(name);
//        } catch (SQLException e) {
//            throw new AssertionError(e);
//        }
//    }
}
