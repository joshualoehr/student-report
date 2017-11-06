# student-report
Simple program for printing a student report - uses JDBC, MySQL. 

Requires access to MySQL database at `mysql.cs.wwu.edu:3306/loehrj`.

Accepts as input a student ID, which is used to query a MySQL database, and outputs the student's name, transcript, and GPA. GPA is calculated via the stored procedure in `calculate_gpa_proc.sql`.

Database parameters are loaded from `db.properties`.

Example `db.properties` file:
```
user=loehrj
password=[redacted]
url=jdbc:mysql://mysql.cs.wwu.edu:3306/loehrj?autoReconnect=true&useSSL=false
```

Example output:
```
Enter ID: 12345
Student #12345 Shankar              
CS-101   Intro. to Computer Science                         Fall   2009 C  4  
CS-190   Game Design                                        Spring 2009 A  4  
CS-315   Robotics                                           Spring 2010 A  3  
CS-347   Database System Concepts                           Fall   2009 A  3  
GPA: 3.43

Enter ID: 19991
Student #19991 Brandt               
HIS-351  World History                                      Spring 2010 B  3  
GPA: 3.00
```
