# SAHRA – Group Night-Out Decision System

COE 418 Database Systems project. Java Swing app + MySQL database.

A group of friends creates a **SAHRA** (a night-out plan) and picks filters: activity type, area, atmosphere and a budget range. SAHRA searches the database for matching places and the group votes. The place with the most votes wins. If nothing matches, SAHRA shows the closest places, starting from the group's **minimum** budget.

## Folder contents

| Path | What it is |
|---|---|
| `sql/schema.sql` | Creates the `sahra` database and every table from the LDM |
| `sql/sample_data.sql` | Test users, groups, 34 activities, 3 example SAHRAs with votes |
| `sql/queries.sql` | Test queries for the report (selection, projection, joins, GROUP BY, fallback, insert/update/delete, constraint tests) |
| `src/sahra/Main.java` | Starts the app |
| `src/sahra/db/` | All database code (JDBC + SQL) |
| `src/sahra/ui/` | All screens (Swing). `Theme.java` holds the colors, fonts and custom-drawn components |
| `lib/` | MySQL driver (Connector/J `.jar`) and FlatLaf (`flatlaf-x.x.jar`, the modern look) |
| `config.properties` | Your MySQL password (not uploaded to GitHub) |

## How to run

### 1. Create the database (once)
1. Open **MySQL Workbench** and connect to your local server.
2. **File → Open SQL Script** → `sql/schema.sql` → click the **lightning bolt** (Execute all).
3. Do the same for `sql/sample_data.sql`.
4. In the left "Schemas" panel, click refresh. You should see `sahra` with its tables.

To reset everything later, run both files again.

### 2. Put your MySQL password in the config (once)
Open `config.properties` and replace `PUT_YOUR_MYSQL_PASSWORD_HERE` with your MySQL root password. Save.

### 3. Run the app
Open `src/sahra/Main.java` in VS Code and click **Run** above `public static void main`. You can also press **F5**.

Log in with **maria@sahra.com / 1234**. Every test account uses password `1234`: maria, karim, lea, omar, nour and jad, all `@sahra.com`.

## Things to try (good for the demo)
- **My groups & SAHRAs** → "LAU Squad" → open **Saturday SAHRA** → vote, then log in as another member and vote.
- **New SAHRA** → Restaurant, Beirut, Lively, $15–$30 → exact matches.
- **New SAHRA** → Restaurant, Byblos, Chill, $15–$25 → no exact match, so fallback results appear.
- **Close voting & pick winner**: only the SAHRA's creator can do this.
- **Browse & manage activities** → filter by any combination, then add, edit or delete an activity. Try deleting "Jounieh Bowl": it's a decided SAHRA's final choice, so the foreign key blocks the delete.

## Database rules (constraints) used
- Primary keys on every table; composite keys on `group_member`, `event_result`, `vote`
- Foreign keys with `ON DELETE CASCADE` where child rows should disappear with the parent, and the default `RESTRICT` where history must be kept
- `UNIQUE` email; `UNIQUE (eventID, userID)` in `vote` = one vote per person per SAHRA
- `vote (eventID, activityID)` → `event_result`: you can only vote for a proposed place
- `CHECK`: allowed areas, atmospheres, types and statuses; `minBudget <= maxBudget`; prices ≥ 0; concert ticket sales before the concert date
- Passwords stored as SHA-256 hashes
