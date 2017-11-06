package loehrj;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * Simple program for printing a student report.
 * 
 * Accepts as input a student ID, which is used to query a MySQL database
 * at mysql.cs.wwu.edu:3306/loehrj, and outputs the student's name, transcript,
 * and GPA. Database parameters are loaded from db.properties.
 * 
 * November 5, 2017
 * @author loehrj
 */

public class StudentReport {
    
    private static final boolean DEBUG = false;
    private static final String ERR = "ERROR";

    private static Scanner scan;
    
    /* MySQL DB Params */
    private static String url;
    private static String user;
    private static String password;
    
    /* SIDs for Debugging */
    private static final List<String> SIDS = new LinkedList<String>(Arrays.asList(
            "00128","12345","19991","23121","44553","45678",
            "54321","55739","70557","76543","76653","98765","98988"
    ));
    
    /* SQL Templates */
    private static String GET_NAME_SQL_FMT = "SELECT name FROM student WHERE id=%s";
    private static String GET_TRANSCRIPT_SQL_FMT = "SELECT A.course_id, B.title,"
            + " A.semester, A.year, A.grade, B.credits FROM takes AS A "
            + "INNER JOIN course AS B ON A.course_id=B.course_id WHERE A.id='%s'";
    private static String CALC_GPA_SQL = "{ CALL calculate_gpa(?,?) }";
    
    /* SQL Keys */
    private static String KEY_NAME = "name";
    private static String KEY_COURSE_ID = "course_id";
    private static String KEY_TITLE = "title";
    private static String KEY_SEMESTER = "semester";
    private static String KEY_YEAR = "year";
    private static String KEY_GRADE = "grade";
    private static String KEY_CREDITS = "credits";
    
    private static HashMap<String, Integer> FIXED_LENGTH;
    
    static {
        scan = new Scanner(System.in);
        
        /* Load DB Properties */
        try (FileInputStream f = new FileInputStream("db.properties")) {
            Properties props = new Properties();
            props.load(f);
            
            url      = props.getProperty("url");
            user     = props.getProperty("user");
            password = props.getProperty("password");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        
        /* Map DB Keys to their max allowed sizes */
        FIXED_LENGTH = new HashMap<String, Integer>();
        FIXED_LENGTH.put(KEY_NAME, 20);
        FIXED_LENGTH.put(KEY_COURSE_ID, 8);
        FIXED_LENGTH.put(KEY_TITLE, 50);
        FIXED_LENGTH.put(KEY_SEMESTER, 6);
        FIXED_LENGTH.put(KEY_YEAR, 4);
        FIXED_LENGTH.put(KEY_GRADE, 2);
        FIXED_LENGTH.put(KEY_CREDITS, 2);
    }
    
    
    public static void main(String[] args) {
        if (DEBUG) {
            Iterator<String> iter = SIDS.iterator();
            while (iter.hasNext())
                report(iter.next());
        }
        
        do {
            report();
        } while (true);
    }
    
    private static void report() {
        System.out.print("Enter ID: ");
        report(scan.nextLine());
    }
    
    private static void report(String sid) {
        System.out.println(String.format("Student #%s %s", sid, getName(sid)));
        System.out.print(getTranscript(sid));
        System.out.println("GPA: " + getGPA(sid));
        System.out.println();
    }
    
    private static String getName(String sid) {
        String sql = String.format(GET_NAME_SQL_FMT, sid);
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {
            
            if (rs.next())
                return fixedLengthResult(rs, KEY_NAME);
            
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        
        return ERR;
    }
    
    private static String getTranscript(String sid) {
        String sql = String.format(GET_TRANSCRIPT_SQL_FMT, sid);
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {
            
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append(transcriptRow(rs));
            }
            return sb.toString();
            
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        
        return ERR;
    }
    
    private static String transcriptRow(ResultSet rs) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append(fixedLengthResult(rs, KEY_COURSE_ID));
        sb.append(fixedLengthResult(rs, KEY_TITLE));
        sb.append(fixedLengthResult(rs, KEY_SEMESTER));
        sb.append(fixedLengthResult(rs, KEY_YEAR));
        sb.append(fixedLengthResult(rs, KEY_GRADE));
        sb.append(fixedLengthResult(rs, KEY_CREDITS));
        sb.append("\n");
        return sb.toString();
    }
    
    private static String fixedLengthResult(ResultSet rs, String key) throws SQLException {
        String fmt = "%-" + FIXED_LENGTH.get(key) + "s ";
        String res = rs.getString(key);
        res = res != null ? res : "";
        return String.format(fmt, res);
    }
    
    private static String getGPA(String sid) {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             CallableStatement stmt = conn.prepareCall(CALC_GPA_SQL);) {
            
            stmt.setString(1, sid);
            stmt.registerOutParameter(2, Types.DECIMAL);
            stmt.executeUpdate();
            
            return stmt.getString(2);
            
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        
        return ERR;
    }
}
