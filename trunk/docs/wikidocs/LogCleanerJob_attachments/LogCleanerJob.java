package org.quartz.jobs;

import java.io.File;
import java.io.FileFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * This class implements a
 * <a href="http://www.quartz-scheduler.org/">Quartz</a>
 * {@link Job} such that files older than a specified number
 * of days get deleted.
 * <p/>
 * The file's date gets inferred from the filename.
 * <p/>
 * More details are to be found in the documentation of the
 * {@link #execute(org.quartz.JobExecutionContext)} method.
 *
 * @author <a href="mailto:mirko.caserta@nexse.com">Mirko Caserta</a>
 * @see #execute(org.quartz.JobExecutionContext)
 */
public class LogCleanerJob implements Job {
    private static final Log logWriter = LogFactory.getLog(LogCleanerJob.class);

    public static final String DATAMAP_KEY_LOG_DIR = "log-dir";
    public static final String DATAMAP_KEY_DELETE_IF_AGE_GREATER_THAN_DAYS = "delete-if-age-greater-than-days";
    public static final String DATAMAP_KEY_DATE_IN_FILENAME_REGEX = "date-in-filename-regex";
    public static final String DATAMAP_KEY_FILENAME_SIMPLE_DATE_FORMAT_PATTERN = "filename-simple-date-format-pattern";
    public static final String DATAMAP_KEY_DELETE_EMPTY_DIRS = "delete-empty-dirs";

    /**
     * When invoked, this method deletes files found inside a given
     * directory (and, recursively, into the the directory's subdirectories)
     * based on criteria specified in the following parameters which
     * have to be passed through the {@link JobDataMap} instance:
     * <p/>
     * <dl>
     * <dt>{@link #DATAMAP_KEY_LOG_DIR}, type: String</dt>
     * <dd>the absolute path to the log base directory</dd>
     * <dt>{@link #DATAMAP_KEY_DELETE_IF_AGE_GREATER_THAN_DAYS}, type: int in a String</dt>
     * <dd>if a file's age is older than the specified number of days, the
     * file gets deleted</dd>
     * <dt>{@link #DATAMAP_KEY_DATE_IN_FILENAME_REGEX}, type: String</dt>
     * <dd>the regular expression to be used to infer the date from a filename;
     * the date has to be matched such that <code>matcher.group(1)</code> returns
     * the date as a {@link String}; see also: {@link Matcher#group(int)}</dd>
     * <dt>{@link #DATAMAP_KEY_FILENAME_SIMPLE_DATE_FORMAT_PATTERN}, type: String</dt>
     * <dd>the pattern to be used in {@link SimpleDateFormat#SimpleDateFormat(String)} to parse
     * the date contained in the matching portion of a filename</dd>
     * <dt>{@link #DATAMAP_KEY_DELETE_EMPTY_DIRS}, type: boolean in a String</dt>
     * <dd>if set to <code>true</code>, after having deleted the matching files, a scan is made
     * for empty directories and found ones will be deleted; please note that the log
     * base dir itself won't get deleted even if it is found to be empty, this is
     * intentional: you do not want to prune the log directory itself unless you want
     * the application server and a bunch of Project Managers to get angry at you</dt>
     * </dl>
     * <p/>
     * <h2>Practical example</h2>
     * <p/>
     * Suppose you have a log directory with an absolute path of:
     * <code>/home/bea/user_projects/domains/mydomain/logs</code>
     * <p/>
     * The log files are named so that they end with the date
     * the last time the log file was accessed. An example file
     * name could be: <code>myserver.log.2005-12-02</code>
     * <p/>
     * The Grim Reaper should delete files older than 60 days.
     * <p/>
     * The following parameters should be used:
     * <p/>
     * <dl>
     * <dt>log-dir</dt>
     * <dd>/home/bea/user_projects/domains/myDomain/logs</dd>
     * <dt>delete-if-age-greater-than-days</dt>
     * <dd>60</dd>
     * <dt>date-in-filename-regex</dt>
     * <dd>^.+\.log\.(\d{4}-\d{2}-\d{2})$</dd>
     * <dt>filename-simple-date-format-pattern</dt>
     * <dd>yyyy-MM-dd</dd>
     * <dt>delete-empty-dirs</dt>
     * <dd>false</dd>
     * </dl>
     *
     * @param context the Quartz context
     * @throws JobExecutionException
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        final JobDataMap jobDataMap = context.getMergedJobDataMap();

        if (jobDataMap == null || jobDataMap.isEmpty()) {
            throw new JobExecutionException("the job data map is null or empty");
        }

        final String logDir = jobDataMap.getString(DATAMAP_KEY_LOG_DIR);

        if (logDir == null || logDir.trim().length() == 0) {
            throw new JobExecutionException(DATAMAP_KEY_LOG_DIR + " is null or empty");
        }

        final File logDirFile = new File(logDir);

        if (!logDirFile.isDirectory()) {
            throw new JobExecutionException(DATAMAP_KEY_LOG_DIR + " is not a directory");
        }

        final int deleteIfAgeGreaterThanDays = jobDataMap.getIntFromString(DATAMAP_KEY_DELETE_IF_AGE_GREATER_THAN_DAYS);

        final GregorianCalendar gc = new GregorianCalendar();
        gc.add(Calendar.DAY_OF_MONTH, - deleteIfAgeGreaterThanDays);
        final Date deleteIfBeforeThisDate = gc.getTime();

        final String dateInFileNameRegex = jobDataMap.getString(DATAMAP_KEY_DATE_IN_FILENAME_REGEX);

        if (dateInFileNameRegex == null || dateInFileNameRegex.trim().length() == 0) {
            throw new JobExecutionException(DATAMAP_KEY_DATE_IN_FILENAME_REGEX + " is null or empty");
        }

        final String dateFormatterPattern = jobDataMap.getString(DATAMAP_KEY_FILENAME_SIMPLE_DATE_FORMAT_PATTERN);

        if (dateFormatterPattern == null || dateFormatterPattern.trim().length() == 0) {
            throw new JobExecutionException(DATAMAP_KEY_FILENAME_SIMPLE_DATE_FORMAT_PATTERN + " is null or empty");
        }

        deleteSelectedFiles(logDirFile, deleteIfBeforeThisDate, dateInFileNameRegex, dateFormatterPattern);

        if (jobDataMap.getBooleanValueFromString(DATAMAP_KEY_DELETE_EMPTY_DIRS)) {
            deleteEmptyDirsRecursively(logDirFile);
        }
    }

    private static void deleteSelectedFiles(File baseDir, Date deleteIfBeforeThisDate, String dateInFileNameRegex, String dateFormatterPattern) throws JobExecutionException {
        logWriter.debug("deleteSelectedFiles: baseDir=" + baseDir + ", deleteIfBeforeThisDate=" + deleteIfBeforeThisDate + ", dateInFileNameRegex=" + dateInFileNameRegex + ", dateFormatterPattern=" + dateFormatterPattern);
        final List fileList = getFileListRecursively(baseDir);
        final Iterator fileListIterator = fileList.iterator();
        final Pattern p = Pattern.compile(dateInFileNameRegex);
        final SimpleDateFormat format = new SimpleDateFormat(dateFormatterPattern);
        long successfullyDeletedFileCount = 0L;
        long failedToDeleteFileCount = 0L;
        long notToBeDeletedFileCount = 0L;

        try {
            while (fileListIterator.hasNext()) {
                final File f = (File) fileListIterator.next();
                final String fileName = f.getName();
                final Matcher m = p.matcher(fileName);

                if (m.matches() && m.groupCount() == 1) {
                    logWriter.debug("fileName matches: fileName=" + fileName);
                    final String dateString = m.group(1);
                    logWriter.debug("inferred date from fileName: dateString=" + dateString);
                    final Date parsedDate = format.parse(dateString);
                    logWriter.debug("inferred date as parsed by SimpleDateFormat: parsedDate=" + parsedDate);

                    if (parsedDate.before(deleteIfBeforeThisDate)) {
                        logWriter.debug("file's date is before specified trigger date, file will be deleted: file=" + f + ", deleteIfBeforeThisDate=" + deleteIfBeforeThisDate);
                        if (f.delete()) {
                            successfullyDeletedFileCount++;
                            logWriter.info("successfully deleted file because it was older than specified date: file=" + f + ", deleteIfBeforeThisDate=" + deleteIfBeforeThisDate);
                        } else {
                            failedToDeleteFileCount++;
                            logWriter.warn("unable to delete file: file=" + f.toString());
                        }
                    } else {
                        notToBeDeletedFileCount++;
                        logWriter.debug("file's date is after specified trigger date, file will NOT be deleted: file=" + f + ", deleteIfBeforeThisDate=" + deleteIfBeforeThisDate);
                    }
                }
            }
        } catch (ParseException ex) {
            throw new JobExecutionException("error parsing file date", ex, false);
        }
        logWriter.debug("deleteSelectedFiles: successfullyDeletedFileCount=" + successfullyDeletedFileCount + ", failedToDeleteFileCount=" + failedToDeleteFileCount + ", notToBeDeletedFileCount=" + notToBeDeletedFileCount);
    }

    private static List getFileListRecursively(File baseDir) {
        final List fileList = new ArrayList();
        getFileListRecursively(baseDir, fileList);
        return fileList;
    }

    private static void getFileListRecursively(File baseDir, List fileList) {
        if (!baseDir.isDirectory()) {
            return;
        }

        final File[] fileArray = baseDir.listFiles();

        if (fileArray == null || fileArray.length == 0) {
            return;
        }

        for (int i = 0; i < fileArray.length; i++) {
            if (fileArray[i].isDirectory()) {
                getFileListRecursively(fileArray[i], fileList); // recurse
            } else if (fileArray[i].isFile()) {
                fileList.add(fileArray[i]);
            }
        }
    }

    private static void deleteEmptyDirsRecursively(File baseDir) {
        logWriter.debug("deleteEmptyDirsRecursively: baseDir=" + baseDir);

        final List dirList = new ArrayList();
        getDirListRecursively(baseDir, dirList);
        final Iterator i = dirList.iterator();
        long successfullyDeletedDirCount = 0L;
        long failedToDeleteDirCount = 0L;
        long notToBeDeletedDirCount = 0L;

        while (i.hasNext()) {
            final File dir = (File) i.next();
            if (isEmptyDir(dir)) {
                if (dir.delete()) {
                    successfullyDeletedDirCount++;
                    logWriter.info("successfully deleted empty dir: dir=" + dir);
                } else {
                    failedToDeleteDirCount++;
                    logWriter.warn("unable to delete empty dir: dir=" + dir);
                }
            } else {
                notToBeDeletedDirCount++;
                logWriter.debug("the directory is not empty and will not be deleted: dir=" + dir);
            }
        }
        logWriter.debug("deleteEmptyDirsRecursively: successfullyDeletedDirCount=" + successfullyDeletedDirCount + ", failedToDeleteDirCount=" + failedToDeleteDirCount + ", notToBeDeletedDirCount=" + notToBeDeletedDirCount);
    }

    private static void getDirListRecursively(File baseDir, List dirList) {
        if (!baseDir.isDirectory()) {
            return;
        }

        final File[] dirArray = baseDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        if (dirArray == null || dirArray.length == 0) {
            return;
        }

        for (int i = 0; i < dirArray.length; i++) {
            dirList.add(dirArray[i]);
            getDirListRecursively(dirArray[i], dirList); // recurse
        }
    }

    private static boolean isEmptyDir(File dir) {
        if (!dir.isDirectory()) {
            return false;
        }
        final File[] fileArray = dir.listFiles();
        return fileArray != null && fileArray.length == 0;
    }

}
