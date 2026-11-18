-- =====================================================================
--  SAHRA - File 2 of 3: SAMPLE DATA (run after schema.sql)
--  Every test account's password is: 1234
--  (stored as its SHA-256 hash, the same way the app stores passwords)
--  Venue names and prices are made up for testing.
-- =====================================================================
USE sahra;

-- ---------------- Users ----------------
INSERT INTO user (userID, fullName, email, password) VALUES
(1, 'Maria Khoury',   'maria@sahra.com', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4'),
(2, 'Karim Haddad',   'karim@sahra.com', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4'),
(3, 'Lea Saade',      'lea@sahra.com',   '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4'),
(4, 'Omar Fakhoury',  'omar@sahra.com',  '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4'),
(5, 'Nour Abi Nader', 'nour@sahra.com',  '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4'),
(6, 'Jad Rizk',       'jad@sahra.com',   '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4');

-- ---------------- Groups ----------------
INSERT INTO friend_group (groupID, groupName, createdBy) VALUES
(1, 'LAU Squad',    1),
(2, 'Weekend Crew', 6);

INSERT INTO group_member (groupID, userID) VALUES
(1,1),(1,2),(1,3),(1,4),(1,5),
(2,1),(2,6);

-- ---------------- Activities (super-type) ----------------
INSERT INTO activity (activityID, name, area, address, atmosphere, averagePrice, phoneNb) VALUES
-- Restaurants (1-10)
(1,  'Sajj & Co',              'Beirut',  'Hamra Main St',           'Lively', 22.00, '01-345678'),
(2,  'Mezze Republic',         'Beirut',  'Gemmayzeh, Gouraud St',   'Lively', 28.00, '01-567123'),
(3,  'Le Petit Cedre',         'Beirut',  'Downtown, Foch St',       'Fancy',  65.00, '01-998877'),
(4,  'Burger Lab',             'Beirut',  'Achrafieh, Sassine Sq',   'Chill',  14.00, '01-223344'),
(5,  'Mar Mikhael Grill',      'Beirut',  'Mar Mikhael, Armenia St', 'Lively', 25.00, '01-441122'),
(6,  'Bay View Fish',          'Jounieh', 'Jounieh Bay Road',        'Fancy',  55.00, '09-912345'),
(7,  'Kaslik Pizza House',     'Jounieh', 'Kaslik Main Road',        'Chill',  16.00, '09-634512'),
(8,  'Old Port Tavern',        'Byblos',  'Byblos Old Port',         'Lively', 30.00, '09-540321'),
(9,  'Batroun Sunset Kitchen', 'Batroun', 'Batroun Coastal Road',    'Chill',  24.00, '06-741258'),
(10, 'Seaside Sushi',          'Batroun', 'Batroun Old Souk',        'Fancy',  48.00, '06-745566'),
-- Cinemas (11-14)
(11, 'City Screens',           'Beirut',  'Verdun, Rashid Karami St','Chill',   9.00, '01-800900'),
(12, 'Platinum VIP Cinema',    'Beirut',  'Downtown Waterfront',     'Fancy',  22.00, '01-800901'),
(13, 'Jounieh Movie Hall',     'Jounieh', 'Jounieh Highway Mall',    'Chill',   8.00, '09-800902'),
(14, 'Byblos Cine Club',       'Byblos',  'Byblos Souk Entrance',    'Chill',   7.00, '09-800903'),
-- Parties (15-19)
(15, 'Neon Rooftop Night',     'Beirut',  'Mar Mikhael Rooftop',     'Lively', 35.00, '03-111222'),
(16, 'Black Tie Gala',         'Beirut',  'Downtown Grand Hall',     'Fancy',  60.00, '03-111333'),
(17, 'Jounieh Bay Party',      'Jounieh', 'Jounieh Marina',          'Lively', 30.00, '03-111444'),
(18, 'Batroun Beach Party',    'Batroun', 'Batroun Public Beach',    'Lively', 25.00, '03-111555'),
(19, 'Byblos Old Town Night',  'Byblos',  'Byblos Old Souk Square',  'Lively', 28.00, '03-111666'),
-- Concerts (20-23)
(20, 'Live at the Forum',      'Beirut',  'Beirut Forum, Karantina', 'Lively', 45.00, '01-700100'),
(21, 'Byblos Summer Stage',    'Byblos',  'Byblos Seafront Stage',   'Fancy',  90.00, '09-700200'),
(22, 'Jounieh Live Arena',     'Jounieh', 'Jounieh Sports Arena',    'Lively', 40.00, '09-700300'),
(23, 'Batroun Acoustic Night', 'Batroun', 'Batroun Lighthouse Park', 'Chill',  30.00, '06-700400'),
-- Karting (24-26)
(24, 'Speedway Beirut',        'Beirut',  'Beirut Seaside Track',    'Lively', 25.00, '01-600100'),
(25, 'Kart Zone Jounieh',      'Jounieh', 'Zouk Mosbeh Industrial',  'Lively', 30.00, '09-600200'),
(26, 'Batroun Karting Club',   'Batroun', 'Batroun Highway Exit',    'Lively', 22.00, '06-600300'),
-- Paintball (27-28)
(27, 'Splat Arena',            'Jounieh', 'Ghazir Hills',            'Lively', 20.00, '09-500100'),
(28, 'Byblos Paintball Park',  'Byblos',  'Amchit Forest Road',      'Lively', 18.00, '09-500200'),
-- Bowling (29-31)
(29, 'Strike Lanes',           'Beirut',  'Hamra, Bliss St',         'Chill',  12.00, '01-400100'),
(30, 'Jounieh Bowl',           'Jounieh', 'Jounieh Highway Mall',    'Lively', 15.00, '09-400200'),
(31, 'Cosmic Bowling',         'Beirut',  'Dbayeh Waterfront',       'Lively', 14.00, '04-400300'),
-- Escape rooms (32-34)
(32, 'Lock & Key',             'Beirut',  'Achrafieh, Monot St',     'Chill',  20.00, '01-300100'),
(33, 'Mystery Vault',          'Jounieh', 'Kaslik Main Road',        'Chill',  18.00, '09-300200'),
(34, 'The Phoenician Puzzle',  'Byblos',  'Byblos Old Souk',         'Lively', 22.00, '09-300300');

-- ---------------- Activity sub-types ----------------
INSERT INTO restaurant (activityID, cuisineType, hasOutdoorSeating, reservationRequired) VALUES
(1,  'Lebanese',      TRUE,  FALSE),
(2,  'Lebanese Mezze',TRUE,  TRUE),
(3,  'French',        FALSE, TRUE),
(4,  'American',      FALSE, FALSE),
(5,  'Grill',         TRUE,  FALSE),
(6,  'Seafood',       TRUE,  TRUE),
(7,  'Italian',       FALSE, FALSE),
(8,  'Mediterranean', TRUE,  FALSE),
(9,  'Lebanese',      TRUE,  FALSE),
(10, 'Japanese',      TRUE,  TRUE);

INSERT INTO cinema (activityID, timeAvailability, filmsShowing, roomType, seatsAvailability) VALUES
(11, '13:00 - 00:30', 'Latest releases', 'Standard', 180),
(12, '16:00 - 01:00', 'Latest releases', 'VIP',       40),
(13, '14:00 - 23:30', 'Latest releases', 'IMAX',     220),
(14, '18:00 - 23:00', 'Classics and indie films', 'Standard', 60);

INSERT INTO party (activityID, eventDate, musicGenre, minAge) VALUES
(15, '2026-11-21', 'House',      18),
(16, '2026-12-05', 'Jazz',       21),
(17, '2026-11-28', 'Arabic Pop', 18),
(18, '2026-11-22', 'Techno',     18),
(19, '2026-11-29', 'Oriental',   18);

INSERT INTO concert (activityID, artistPerforming, musicGenre, eventDate, showStartTime, ticketSaleStartDate) VALUES
(20, 'The Cedar Band',      'Rock',      '2026-11-27', '21:00:00', '2026-10-15'),
(21, 'Beirut Symphony',     'Classical', '2026-12-12', '20:30:00', '2026-10-01'),
(22, 'DJ Layla',            'Electronic','2026-11-21', '22:00:00', '2026-11-01'),
(23, 'Acoustic Duo Zaatar', 'Acoustic',  '2026-11-22', '20:00:00', '2026-11-05');

INSERT INTO karting (activityID, isReservationRequired, minAge) VALUES
(24, FALSE, 12),
(25, TRUE,  14),
(26, FALSE, 10);

INSERT INTO paintball (activityID, environmentType, numberOfFields, maxPlayers, isReservationRequired) VALUES
(27, 'Outdoor', 3, 30, TRUE),
(28, 'Both',    2, 20, TRUE);

INSERT INTO bowling (activityID, offersFoodService, shoeRentalIncluded) VALUES
(29, FALSE, TRUE),
(30, TRUE,  TRUE),
(31, TRUE,  FALSE);

INSERT INTO escape_room (activityID, themesAvailable, bookingRequired, lobbyCapacity, difficultyLevel) VALUES
(32, 'Bank Heist, Haunted House', TRUE, 8,  'Medium'),
(33, 'Pirate Ship, Lab Escape',   TRUE, 6,  'Hard'),
(34, 'Ancient Phoenicia',         TRUE, 10, 'Easy');

-- ---------------- Events (SAHRAs) ----------------
-- Event 1: still voting, exact matches found
INSERT INTO event (eventID, groupID, finalActivityID, createdBy, title, plannedDate, status,
                   area, activityType, atmosphere, minBudget, maxBudget, isFallback) VALUES
(1, 1, NULL, 1, 'Saturday SAHRA', '2026-11-21', 'VOTING',
 'Beirut', 'Restaurant', 'Lively', 15.00, 30.00, FALSE);

INSERT INTO event_result (eventID, activityID, matchType, displayRank) VALUES
(1, 1, 'EXACT', 1),
(1, 5, 'EXACT', 2),
(1, 2, 'EXACT', 3);

INSERT INTO vote (eventID, activityID, userID) VALUES
(1, 1, 1),
(1, 1, 2),
(1, 2, 3);

-- Event 2: decided, exact match
INSERT INTO event (eventID, groupID, finalActivityID, createdBy, title, plannedDate, status,
                   area, activityType, atmosphere, minBudget, maxBudget, isFallback) VALUES
(2, 1, 30, 2, 'Exams Are Over', '2026-11-07', 'DECIDED',
 'Jounieh', 'Bowling', 'Lively', 10.00, 20.00, FALSE);

INSERT INTO event_result (eventID, activityID, matchType, displayRank) VALUES
(2, 30, 'EXACT', 1);

INSERT INTO vote (eventID, activityID, userID) VALUES
(2, 30, 1), (2, 30, 2), (2, 30, 3), (2, 30, 4);

-- Event 3: decided, NO exact match -> fallback results
-- (Chill restaurant in Byblos for $15-$25 does not exist)
INSERT INTO event (eventID, groupID, finalActivityID, createdBy, title, plannedDate, status,
                   area, activityType, atmosphere, minBudget, maxBudget, isFallback) VALUES
(3, 2, 8, 6, 'Byblos Dinner', '2026-11-08', 'DECIDED',
 'Byblos', 'Restaurant', 'Chill', 15.00, 25.00, TRUE);

INSERT INTO event_result (eventID, activityID, matchType, displayRank) VALUES
(3, 8, 'FALLBACK', 1),
(3, 4, 'FALLBACK', 2),
(3, 7, 'FALLBACK', 3),
(3, 9, 'FALLBACK', 4),
(3, 1, 'FALLBACK', 5);

INSERT INTO vote (eventID, activityID, userID) VALUES
(3, 8, 1), (3, 8, 6);

-- keep AUTO_INCREMENT counters after the hand-picked IDs
ALTER TABLE user         AUTO_INCREMENT = 100;
ALTER TABLE friend_group AUTO_INCREMENT = 100;
ALTER TABLE activity     AUTO_INCREMENT = 100;
ALTER TABLE event        AUTO_INCREMENT = 100;
