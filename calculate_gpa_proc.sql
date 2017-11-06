CREATE DEFINER=`loehrj`@`%` PROCEDURE `calculate_gpa`(IN sid VARCHAR(5), OUT gpa DECIMAL(3,2))
BEGIN
	DECLARE done INT DEFAULT 0;
    DECLARE credits DECIMAL(2,0) DEFAULT 0.0;
    DECLARE grade VARCHAR(2);
    DECLARE gp, total_gp DECIMAL (3,1) DEFAULT 0.0;
    DECLARE total_credits DECIMAL(3,0) DEFAULT 0.0;
    
	DECLARE cur CURSOR FOR
		SELECT B.credits, A.grade FROM takes AS A
		INNER JOIN course AS B ON A.course_id=B.course_id
		WHERE A.ID=sid AND A.grade IS NOT NULL;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
    
    SELECT SUM(B.credits) INTO total_credits
		FROM takes AS A, course AS B
		WHERE A.course_id=B.course_id AND A.ID=sid AND A.grade IS NOT NULL;
    
    SET gpa = 0.0;
    
    OPEN cur;
    
    read_loop: LOOP
		FETCH cur INTO credits, grade;
        IF done THEN
			LEAVE read_loop;
		END IF;
        
        CASE grade
			WHEN 'A' THEN SET gp = 4.0;
            WHEN 'A-' THEN SET gp = 3.5;
            WHEN 'B+' THEN SET gp = 3.3;
            WHEN 'B' THEN SET gp = 3.0;
            WHEN 'B-' THEN SET gp = 2.5;
            WHEN 'C+' THEN SET gp = 2.3;
            WHEN 'C' THEN SET gp = 2.0;
            WHEN 'C-' THEN SET gp = 1.5;
            WHEN 'D+' THEN SET gp = 1.3;
            WHEN 'D' THEN SET gp = 1.0;
            WHEN 'D-' THEN SET gp = 0.5;
            WHEN 'F' THEN SET gp = 0.0;
        END CASE;
        SET total_gp = total_gp + gp * credits;
	END LOOP;
    
	CLOSE cur;
    
    IF total_credits > 0.0 THEN
		SET gpa = total_gp / total_credits;
	END IF;
END