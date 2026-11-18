-- =====================================================================
--  SAHRA - File 3 of 3: TEST QUERIES (expert-user queries for the report)
--  Run them one at a time in MySQL Workbench: put the cursor on a query
--  and press Ctrl+Enter. Screenshot the result for your report.
--
--  Queries that CHANGE data are wrapped in START TRANSACTION ... ROLLBACK
--  so your sample data stays the same afterwards.
-- =====================================================================
USE sahra;

-- ---------------------------------------------------------------------
-- A. SELECTION (sigma) - rows that satisfy a condition
-- ---------------------------------------------------------------------
-- A1. All activities in Beirut
SELECT * FROM activity WHERE area = 'Beirut';

-- A2. Activities cheaper than $20 per person
SELECT * FROM activity WHERE averagePrice < 20;

-- ---------------------------------------------------------------------
-- B. PROJECTION (pi) - only some columns
-- ---------------------------------------------------------------------
-- B1. Just name and price of every activity
SELECT name, averagePrice FROM activity;

-- B2. Distinct areas that have at least one activity
SELECT DISTINCT area FROM activity;

-- ---------------------------------------------------------------------
-- C. CONJUNCTIVE (AND) and DISJUNCTIVE (OR) queries
-- ---------------------------------------------------------------------
-- C1. The core SAHRA search: Restaurant + Beirut + Lively + $15-$30
SELECT activityID, name, area, atmosphere, averagePrice
FROM activity_with_type
WHERE activityType = 'Restaurant'
  AND area = 'Beirut'
  AND atmosphere = 'Lively'
  AND averagePrice BETWEEN 15 AND 30
ORDER BY averagePrice;

-- C2. Anything in Byblos OR Batroun
SELECT name, area, activityType FROM activity_with_type
WHERE area = 'Byblos' OR area = 'Batroun';

-- C3. Chill places, OR anything under $10 (mixed AND/OR)
SELECT name, atmosphere, averagePrice FROM activity
WHERE atmosphere = 'Chill' OR averagePrice < 10;

-- ---------------------------------------------------------------------
-- D. JOINS across multiple tables
-- ---------------------------------------------------------------------
-- D1. Restaurants with their cuisine (activity JOIN restaurant)
SELECT a.name, a.area, r.cuisineType, r.hasOutdoorSeating
FROM activity a
JOIN restaurant r ON r.activityID = a.activityID;

-- D2. Members of each group (3-table join)
SELECT g.groupName, u.fullName, u.email, gm.joinedAt
FROM friend_group g
JOIN group_member gm ON gm.groupID = g.groupID
JOIN user u          ON u.userID   = gm.userID
ORDER BY g.groupName, u.fullName;

-- D3. Who voted for what in each event (4-table join)
SELECT e.title, u.fullName AS voter, a.name AS votedFor, v.votedAt
FROM vote v
JOIN event e    ON e.eventID    = v.eventID
JOIN user u     ON u.userID     = v.userID
JOIN activity a ON a.activityID = v.activityID
ORDER BY e.eventID, u.fullName;

-- D4. Final decision of every decided event (LEFT JOIN keeps undecided ones)
SELECT e.title, e.status, a.name AS finalChoice
FROM event e
LEFT JOIN activity a ON a.activityID = e.finalActivityID;

-- ---------------------------------------------------------------------
-- E. AGGREGATION: GROUP BY, COUNT, AVG, HAVING, ORDER BY
-- ---------------------------------------------------------------------
-- E1. Vote count per proposed activity in event 1 (the voting tally)
SELECT a.name, COUNT(v.userID) AS votes
FROM event_result er
JOIN activity a   ON a.activityID = er.activityID
LEFT JOIN vote v  ON v.eventID = er.eventID AND v.activityID = er.activityID
WHERE er.eventID = 1
GROUP BY er.activityID, a.name, a.averagePrice
ORDER BY votes DESC, a.averagePrice ASC;

-- E2. Winner of event 1 (top of the tally; ties go to the cheaper place)
SELECT a.name, COUNT(v.userID) AS votes
FROM event_result er
JOIN activity a   ON a.activityID = er.activityID
LEFT JOIN vote v  ON v.eventID = er.eventID AND v.activityID = er.activityID
WHERE er.eventID = 1
GROUP BY er.activityID, a.name, a.averagePrice
ORDER BY votes DESC, a.averagePrice ASC
LIMIT 1;

-- E3. Number of activities and average price per type
SELECT activityType, COUNT(*) AS howMany, ROUND(AVG(averagePrice), 2) AS avgPrice
FROM activity_with_type
GROUP BY activityType
ORDER BY avgPrice;

-- E4. Areas with more than 5 activities (HAVING)
SELECT area, COUNT(*) AS howMany
FROM activity
GROUP BY area
HAVING COUNT(*) > 5;

-- E5. Group members who have NOT voted yet in event 1 (NOT EXISTS subquery)
SELECT u.fullName
FROM event e
JOIN group_member gm ON gm.groupID = e.groupID
JOIN user u          ON u.userID   = gm.userID
WHERE e.eventID = 1
  AND NOT EXISTS (SELECT 1 FROM vote v WHERE v.eventID = e.eventID AND v.userID = u.userID);

-- E6. Activities more expensive than the average of their own type (correlated subquery)
SELECT name, activityType, averagePrice
FROM activity_with_type x
WHERE averagePrice > (SELECT AVG(averagePrice) FROM activity_with_type y
                      WHERE y.activityType = x.activityType);

-- ---------------------------------------------------------------------
-- F. BUDGET FALLBACK (when the exact search returns nothing)
-- ---------------------------------------------------------------------
-- F1. Exact search: Chill restaurant in Byblos for $15-$25 -> returns 0 rows
SELECT * FROM activity_with_type
WHERE activityType = 'Restaurant' AND area = 'Byblos'
  AND atmosphere = 'Chill' AND averagePrice BETWEEN 15 AND 25;

-- F2. Fallback: same type, prefer same area, then same atmosphere,
--     then within budget, then closest to the MINIMUM budget ($15)
SELECT name, area, atmosphere, averagePrice
FROM activity_with_type
WHERE activityType = 'Restaurant'
ORDER BY (area = 'Byblos') DESC,
         (atmosphere = 'Chill') DESC,
         (averagePrice > 25) ASC,
         ABS(averagePrice - 15) ASC,
         averagePrice ASC
LIMIT 5;

-- ---------------------------------------------------------------------
-- G. INSERT / UPDATE / DELETE (rolled back afterwards)
-- ---------------------------------------------------------------------
START TRANSACTION;

-- G1. Insert a new café-style restaurant
INSERT INTO activity (name, area, address, atmosphere, averagePrice, phoneNb)
VALUES ('Test Garden Cafe', 'Batroun', 'Batroun Main St', 'Chill', 11.00, '06-000000');
INSERT INTO restaurant (activityID, cuisineType, hasOutdoorSeating, reservationRequired)
VALUES (LAST_INSERT_ID(), 'Cafe', TRUE, FALSE);
SELECT * FROM activity_with_type WHERE name = 'Test Garden Cafe';

-- G2. Raise every Beirut price by 10%
UPDATE activity SET averagePrice = averagePrice * 1.10 WHERE area = 'Beirut';
SELECT name, averagePrice FROM activity WHERE area = 'Beirut';

-- G3. User 3 changes their vote in event 1
UPDATE vote SET activityID = 1 WHERE eventID = 1 AND userID = 3;

-- G4. Delete a user (their memberships and votes are removed by ON DELETE CASCADE)
DELETE FROM user WHERE userID = 5;
SELECT * FROM group_member WHERE userID = 5;   -- returns 0 rows

ROLLBACK;   -- undo everything in this block

-- ---------------------------------------------------------------------
-- H. CONSTRAINT TESTS - each of these SHOULD FAIL with an error.
--    Run them one by one and screenshot the error message.
-- ---------------------------------------------------------------------
-- H1. Duplicate email (UNIQUE)
-- INSERT INTO user (fullName, email, password) VALUES ('Fake', 'maria@sahra.com', 'x');

-- H2. Budget minimum above maximum (CHECK chk_event_budget)
-- INSERT INTO event (groupID, createdBy, title, plannedDate, area, activityType, atmosphere, minBudget, maxBudget)
-- VALUES (1, 1, 'Bad budget', '2026-12-01', 'Beirut', 'Restaurant', 'Chill', 50, 20);

-- H3. Unknown area (CHECK chk_activity_area)
-- INSERT INTO activity (name, area, address, atmosphere, averagePrice) VALUES ('X', 'Tripoli', 'x', 'Chill', 10);

-- H4. Voting twice in the same event (UNIQUE uq_one_vote_per_user)
-- INSERT INTO vote (eventID, activityID, userID) VALUES (1, 5, 1);

-- H5. Voting for an activity that was NOT proposed (FOREIGN KEY fk_vote_result)
-- INSERT INTO vote (eventID, activityID, userID) VALUES (1, 3, 4);

-- H6. Deleting an activity that is an event's final choice (FOREIGN KEY fk_event_final)
-- DELETE FROM activity WHERE activityID = 30;
