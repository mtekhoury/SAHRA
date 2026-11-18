-- =====================================================================
--  SAHRA - Group Night-Out Decision System
--  COE 418 Database Systems - Fall 2026
--  File 1 of 3: SCHEMA (run this first)
--
--  Builds every table from the logical data model (LDM):
--    user, friend_group, group_member, event, activity,
--    8 activity sub-types, event_result, vote
--  plus one helper view (activity_with_type).
--
--  Running this file again DELETES the database and starts over.
-- =====================================================================

DROP DATABASE IF EXISTS sahra;
CREATE DATABASE sahra CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sahra;

-- ---------------------------------------------------------------------
-- USER: registered people
-- ---------------------------------------------------------------------
CREATE TABLE user (
    userID      INT AUTO_INCREMENT PRIMARY KEY,
    fullName    VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    CHAR(64)     NOT NULL,          -- SHA-256 hash, never plain text
    createdAt   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_email CHECK (email LIKE '%_@_%._%')
);

-- ---------------------------------------------------------------------
-- FRIENDGROUP: a group of friends who plan outings together
-- ---------------------------------------------------------------------
CREATE TABLE friend_group (
    groupID     INT AUTO_INCREMENT PRIMARY KEY,
    groupName   VARCHAR(100) NOT NULL,
    createdBy   INT          NOT NULL,
    createdAt   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_group_creator FOREIGN KEY (createdBy) REFERENCES user(userID)
);

-- ---------------------------------------------------------------------
-- GROUP_MEMBER: which users belong to which groups (M:N)
-- ---------------------------------------------------------------------
CREATE TABLE group_member (
    groupID     INT       NOT NULL,
    userID      INT       NOT NULL,
    joinedAt    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (groupID, userID),
    CONSTRAINT fk_member_group FOREIGN KEY (groupID) REFERENCES friend_group(groupID) ON DELETE CASCADE,
    CONSTRAINT fk_member_user  FOREIGN KEY (userID)  REFERENCES user(userID)          ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- ACTIVITY: super-type for every place/thing the group can do
-- ---------------------------------------------------------------------
CREATE TABLE activity (
    activityID   INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100)  NOT NULL,
    area         VARCHAR(30)   NOT NULL,
    address      VARCHAR(200)  NOT NULL,
    atmosphere   VARCHAR(20)   NOT NULL,
    averagePrice DECIMAL(7,2)  NOT NULL,        -- USD per person
    phoneNb      VARCHAR(20),
    CONSTRAINT chk_activity_area       CHECK (area IN ('Beirut','Jounieh','Byblos','Batroun')),
    CONSTRAINT chk_activity_atmosphere CHECK (atmosphere IN ('Chill','Lively','Fancy')),
    CONSTRAINT chk_activity_price      CHECK (averagePrice >= 0)
);

-- ---------------------------------------------------------------------
-- ACTIVITY SUB-TYPES (each row extends exactly one activity, 1:1)
-- ---------------------------------------------------------------------
CREATE TABLE restaurant (
    restaurantID        INT AUTO_INCREMENT PRIMARY KEY,
    activityID          INT NOT NULL UNIQUE,
    cuisineType         VARCHAR(50) NOT NULL,
    hasOutdoorSeating   BOOLEAN NOT NULL DEFAULT FALSE,
    reservationRequired BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_restaurant_activity FOREIGN KEY (activityID) REFERENCES activity(activityID) ON DELETE CASCADE
);

CREATE TABLE cinema (
    cinemaID          INT AUTO_INCREMENT PRIMARY KEY,
    activityID        INT NOT NULL UNIQUE,
    timeAvailability  VARCHAR(50)  NOT NULL,     -- e.g. '14:00 - 00:30'
    filmsShowing      VARCHAR(255) NOT NULL,
    roomType          VARCHAR(20)  NOT NULL,
    seatsAvailability INT NOT NULL,
    CONSTRAINT fk_cinema_activity FOREIGN KEY (activityID) REFERENCES activity(activityID) ON DELETE CASCADE,
    CONSTRAINT chk_cinema_room  CHECK (roomType IN ('Standard','IMAX','VIP','4DX')),
    CONSTRAINT chk_cinema_seats CHECK (seatsAvailability >= 0)
);

CREATE TABLE party (
    partyID     INT AUTO_INCREMENT PRIMARY KEY,
    activityID  INT NOT NULL UNIQUE,
    eventDate   DATE NOT NULL,
    musicGenre  VARCHAR(50) NOT NULL,
    minAge      INT NOT NULL DEFAULT 18,
    CONSTRAINT fk_party_activity FOREIGN KEY (activityID) REFERENCES activity(activityID) ON DELETE CASCADE,
    CONSTRAINT chk_party_age CHECK (minAge BETWEEN 0 AND 30)
);

CREATE TABLE concert (
    concertID           INT AUTO_INCREMENT PRIMARY KEY,
    activityID          INT NOT NULL UNIQUE,
    artistPerforming    VARCHAR(100) NOT NULL,
    musicGenre          VARCHAR(50)  NOT NULL,
    eventDate           DATE NOT NULL,
    showStartTime       TIME NOT NULL,
    ticketSaleStartDate DATE NOT NULL,
    CONSTRAINT fk_concert_activity FOREIGN KEY (activityID) REFERENCES activity(activityID) ON DELETE CASCADE,
    CONSTRAINT chk_concert_dates CHECK (ticketSaleStartDate <= eventDate)
);

CREATE TABLE karting (
    kartingID             INT AUTO_INCREMENT PRIMARY KEY,
    activityID            INT NOT NULL UNIQUE,
    isReservationRequired BOOLEAN NOT NULL DEFAULT FALSE,
    minAge                INT NOT NULL DEFAULT 12,
    CONSTRAINT fk_karting_activity FOREIGN KEY (activityID) REFERENCES activity(activityID) ON DELETE CASCADE,
    CONSTRAINT chk_karting_age CHECK (minAge BETWEEN 0 AND 30)
);

CREATE TABLE paintball (
    paintballID           INT AUTO_INCREMENT PRIMARY KEY,
    activityID            INT NOT NULL UNIQUE,
    environmentType       VARCHAR(20) NOT NULL,
    numberOfFields        INT NOT NULL,
    maxPlayers            INT NOT NULL,
    isReservationRequired BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_paintball_activity FOREIGN KEY (activityID) REFERENCES activity(activityID) ON DELETE CASCADE,
    CONSTRAINT chk_paintball_env     CHECK (environmentType IN ('Indoor','Outdoor','Both')),
    CONSTRAINT chk_paintball_fields  CHECK (numberOfFields > 0),
    CONSTRAINT chk_paintball_players CHECK (maxPlayers > 0)
);

CREATE TABLE bowling (
    bowlingID          INT AUTO_INCREMENT PRIMARY KEY,
    activityID         INT NOT NULL UNIQUE,
    offersFoodService  BOOLEAN NOT NULL DEFAULT FALSE,
    shoeRentalIncluded BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_bowling_activity FOREIGN KEY (activityID) REFERENCES activity(activityID) ON DELETE CASCADE
);

CREATE TABLE escape_room (
    escapeRoomID    INT AUTO_INCREMENT PRIMARY KEY,
    activityID      INT NOT NULL UNIQUE,
    themesAvailable VARCHAR(255) NOT NULL,
    bookingRequired BOOLEAN NOT NULL DEFAULT TRUE,
    lobbyCapacity   INT NOT NULL,
    difficultyLevel VARCHAR(20) NOT NULL,
    CONSTRAINT fk_escape_activity FOREIGN KEY (activityID) REFERENCES activity(activityID) ON DELETE CASCADE,
    CONSTRAINT chk_escape_capacity   CHECK (lobbyCapacity > 0),
    CONSTRAINT chk_escape_difficulty CHECK (difficultyLevel IN ('Easy','Medium','Hard','Expert'))
);

-- ---------------------------------------------------------------------
-- EVENT: one night-out planning session (a "SAHRA") with its filters
-- ---------------------------------------------------------------------
CREATE TABLE event (
    eventID         INT AUTO_INCREMENT PRIMARY KEY,
    groupID         INT NOT NULL,
    finalActivityID INT NULL,                    -- filled when voting closes
    createdBy       INT NOT NULL,
    title           VARCHAR(100) NOT NULL,
    plannedDate     DATE NOT NULL,
    status          VARCHAR(10)  NOT NULL DEFAULT 'VOTING',
    area            VARCHAR(30)  NOT NULL,
    activityType    VARCHAR(20)  NOT NULL,
    atmosphere      VARCHAR(20)  NOT NULL,
    minBudget       DECIMAL(7,2) NOT NULL,
    maxBudget       DECIMAL(7,2) NOT NULL,
    isFallback      BOOLEAN NOT NULL DEFAULT FALSE,  -- TRUE if no exact match was found
    createdAt       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_group   FOREIGN KEY (groupID)         REFERENCES friend_group(groupID) ON DELETE CASCADE,
    CONSTRAINT fk_event_final   FOREIGN KEY (finalActivityID) REFERENCES activity(activityID),
    CONSTRAINT fk_event_creator FOREIGN KEY (createdBy)       REFERENCES user(userID),
    CONSTRAINT chk_event_status CHECK (status IN ('VOTING','DECIDED')),
    CONSTRAINT chk_event_area   CHECK (area IN ('Beirut','Jounieh','Byblos','Batroun')),
    CONSTRAINT chk_event_type   CHECK (activityType IN ('Restaurant','Cinema','Party','Concert',
                                                        'Karting','Paintball','Bowling','Escape Room')),
    CONSTRAINT chk_event_atmos  CHECK (atmosphere IN ('Chill','Lively','Fancy')),
    CONSTRAINT chk_event_budget CHECK (minBudget >= 0 AND minBudget <= maxBudget)
);

-- ---------------------------------------------------------------------
-- EVENT_RESULT: the activities SAHRA proposed for an event
-- ---------------------------------------------------------------------
CREATE TABLE event_result (
    eventID     INT NOT NULL,
    activityID  INT NOT NULL,
    matchType   VARCHAR(10) NOT NULL,            -- EXACT or FALLBACK
    displayRank INT NOT NULL,
    PRIMARY KEY (eventID, activityID),
    CONSTRAINT fk_result_event    FOREIGN KEY (eventID)    REFERENCES event(eventID) ON DELETE CASCADE,
    CONSTRAINT fk_result_activity FOREIGN KEY (activityID) REFERENCES activity(activityID),
    CONSTRAINT chk_result_match   CHECK (matchType IN ('EXACT','FALLBACK')),
    CONSTRAINT chk_result_rank    CHECK (displayRank > 0)
);

-- ---------------------------------------------------------------------
-- VOTE: one vote per user per event, only for a proposed result
-- ---------------------------------------------------------------------
CREATE TABLE vote (
    eventID     INT NOT NULL,
    activityID  INT NOT NULL,
    userID      INT NOT NULL,
    votedAt     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (eventID, activityID, userID),
    CONSTRAINT uq_one_vote_per_user UNIQUE (eventID, userID),
    -- you can only vote for an activity that SAHRA proposed for this event
    CONSTRAINT fk_vote_result FOREIGN KEY (eventID, activityID)
        REFERENCES event_result(eventID, activityID) ON DELETE CASCADE,
    CONSTRAINT fk_vote_user   FOREIGN KEY (userID) REFERENCES user(userID) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- VIEW: every activity with its sub-type name, used by the app's search
-- ---------------------------------------------------------------------
CREATE VIEW activity_with_type AS
SELECT a.*,
       CASE
           WHEN r.activityID  IS NOT NULL THEN 'Restaurant'
           WHEN c.activityID  IS NOT NULL THEN 'Cinema'
           WHEN p.activityID  IS NOT NULL THEN 'Party'
           WHEN co.activityID IS NOT NULL THEN 'Concert'
           WHEN k.activityID  IS NOT NULL THEN 'Karting'
           WHEN pb.activityID IS NOT NULL THEN 'Paintball'
           WHEN b.activityID  IS NOT NULL THEN 'Bowling'
           WHEN e.activityID  IS NOT NULL THEN 'Escape Room'
       END AS activityType
FROM activity a
LEFT JOIN restaurant  r  ON r.activityID  = a.activityID
LEFT JOIN cinema      c  ON c.activityID  = a.activityID
LEFT JOIN party       p  ON p.activityID  = a.activityID
LEFT JOIN concert     co ON co.activityID = a.activityID
LEFT JOIN karting     k  ON k.activityID  = a.activityID
LEFT JOIN paintball   pb ON pb.activityID = a.activityID
LEFT JOIN bowling     b  ON b.activityID  = a.activityID
LEFT JOIN escape_room e  ON e.activityID  = a.activityID;
