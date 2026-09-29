CREATE DATABASE IF NOT EXISTS volunteerhub;
USE volunteerhub;

START TRANSACTION;

INSERT INTO volunteers (name, email, phone)
SELECT 'Avery Smith', 'demo.avery@volunteerhub.local', '+1 555-010-1001'
WHERE NOT EXISTS (
    SELECT 1 FROM volunteers WHERE email = 'demo.avery@volunteerhub.local'
);

INSERT INTO volunteers (name, email, phone)
SELECT 'Jordan Lee', 'demo.jordan@volunteerhub.local', '+1 555-010-1002'
WHERE NOT EXISTS (
    SELECT 1 FROM volunteers WHERE email = 'demo.jordan@volunteerhub.local'
);

INSERT INTO volunteers (name, email, phone)
SELECT 'Casey Morgan', 'demo.casey@volunteerhub.local', '+1 555-010-1003'
WHERE NOT EXISTS (
    SELECT 1 FROM volunteers WHERE email = 'demo.casey@volunteerhub.local'
);

INSERT INTO events (name, description, `date`, location, volunteer_capacity)
SELECT 'Tree Plantation Drive', 'Plant native trees in the community park.', '2026-10-10', 'Community Park', 10
WHERE NOT EXISTS (
    SELECT 1 FROM events
    WHERE name = 'Tree Plantation Drive' AND `date` = '2026-10-10' AND location = 'Community Park'
);

INSERT INTO events (name, description, `date`, location, volunteer_capacity)
SELECT 'Food Donation Drive', 'Sort and distribute donated food.', '2026-10-15', 'Community Food Bank', 8
WHERE NOT EXISTS (
    SELECT 1 FROM events
    WHERE name = 'Food Donation Drive' AND `date` = '2026-10-15' AND location = 'Community Food Bank'
);

INSERT INTO events (name, description, `date`, location, volunteer_capacity)
SELECT 'Riverside Cleanup', 'Collect litter along the riverside trail.', '2026-10-20', 'Riverside Trail', 12
WHERE NOT EXISTS (
    SELECT 1 FROM events
    WHERE name = 'Riverside Cleanup' AND `date` = '2026-10-20' AND location = 'Riverside Trail'
);

INSERT INTO signups (event_id, volunteer_id, signup_date)
SELECT event_row.id, volunteer_row.id, CURRENT_DATE
FROM events event_row
JOIN volunteers volunteer_row ON volunteer_row.email = 'demo.avery@volunteerhub.local'
WHERE event_row.name = 'Tree Plantation Drive'
  AND event_row.`date` = '2026-10-10'
  AND NOT EXISTS (
      SELECT 1 FROM signups existing_signup
      WHERE existing_signup.event_id = event_row.id
        AND existing_signup.volunteer_id = volunteer_row.id
  );

INSERT INTO signups (event_id, volunteer_id, signup_date)
SELECT event_row.id, volunteer_row.id, CURRENT_DATE
FROM events event_row
JOIN volunteers volunteer_row ON volunteer_row.email = 'demo.avery@volunteerhub.local'
WHERE event_row.name = 'Food Donation Drive'
  AND event_row.`date` = '2026-10-15'
  AND NOT EXISTS (
      SELECT 1 FROM signups existing_signup
      WHERE existing_signup.event_id = event_row.id
        AND existing_signup.volunteer_id = volunteer_row.id
  );

INSERT INTO signups (event_id, volunteer_id, signup_date)
SELECT event_row.id, volunteer_row.id, CURRENT_DATE
FROM events event_row
JOIN volunteers volunteer_row ON volunteer_row.email = 'demo.jordan@volunteerhub.local'
WHERE event_row.name = 'Tree Plantation Drive'
  AND event_row.`date` = '2026-10-10'
  AND NOT EXISTS (
      SELECT 1 FROM signups existing_signup
      WHERE existing_signup.event_id = event_row.id
        AND existing_signup.volunteer_id = volunteer_row.id
  );

INSERT INTO signups (event_id, volunteer_id, signup_date)
SELECT event_row.id, volunteer_row.id, CURRENT_DATE
FROM events event_row
JOIN volunteers volunteer_row ON volunteer_row.email = 'demo.casey@volunteerhub.local'
WHERE event_row.name = 'Riverside Cleanup'
  AND event_row.`date` = '2026-10-20'
  AND NOT EXISTS (
      SELECT 1 FROM signups existing_signup
      WHERE existing_signup.event_id = event_row.id
        AND existing_signup.volunteer_id = volunteer_row.id
  );

INSERT INTO attendance_records (signup_id, attended, hours_contributed)
SELECT signup_row.id, TRUE, 4.00
FROM signups signup_row
JOIN events event_row ON event_row.id = signup_row.event_id
JOIN volunteers volunteer_row ON volunteer_row.id = signup_row.volunteer_id
WHERE event_row.name = 'Tree Plantation Drive'
  AND event_row.`date` = '2026-10-10'
  AND volunteer_row.email = 'demo.avery@volunteerhub.local'
  AND NOT EXISTS (
      SELECT 1 FROM attendance_records existing_attendance
      WHERE existing_attendance.signup_id = signup_row.id
  );

INSERT INTO attendance_records (signup_id, attended, hours_contributed)
SELECT signup_row.id, TRUE, 8.00
FROM signups signup_row
JOIN events event_row ON event_row.id = signup_row.event_id
JOIN volunteers volunteer_row ON volunteer_row.id = signup_row.volunteer_id
WHERE event_row.name = 'Food Donation Drive'
  AND event_row.`date` = '2026-10-15'
  AND volunteer_row.email = 'demo.avery@volunteerhub.local'
  AND NOT EXISTS (
      SELECT 1 FROM attendance_records existing_attendance
      WHERE existing_attendance.signup_id = signup_row.id
  );

INSERT INTO attendance_records (signup_id, attended, hours_contributed)
SELECT signup_row.id, FALSE, 0.00
FROM signups signup_row
JOIN events event_row ON event_row.id = signup_row.event_id
JOIN volunteers volunteer_row ON volunteer_row.id = signup_row.volunteer_id
WHERE event_row.name = 'Tree Plantation Drive'
  AND event_row.`date` = '2026-10-10'
  AND volunteer_row.email = 'demo.jordan@volunteerhub.local'
  AND NOT EXISTS (
      SELECT 1 FROM attendance_records existing_attendance
      WHERE existing_attendance.signup_id = signup_row.id
  );

INSERT INTO attendance_records (signup_id, attended, hours_contributed)
SELECT signup_row.id, TRUE, 3.00
FROM signups signup_row
JOIN events event_row ON event_row.id = signup_row.event_id
JOIN volunteers volunteer_row ON volunteer_row.id = signup_row.volunteer_id
WHERE event_row.name = 'Riverside Cleanup'
  AND event_row.`date` = '2026-10-20'
  AND volunteer_row.email = 'demo.casey@volunteerhub.local'
  AND NOT EXISTS (
      SELECT 1 FROM attendance_records existing_attendance
      WHERE existing_attendance.signup_id = signup_row.id
  );

INSERT INTO volunteers (name, email, phone)
SELECT 'Morgan Patel', 'demo.morgan@volunteerhub.local', '+1 555-010-1004'
WHERE NOT EXISTS (SELECT 1 FROM volunteers WHERE email = 'demo.morgan@volunteerhub.local');

INSERT INTO volunteers (name, email, phone)
SELECT 'Taylor Brooks', 'demo.taylor@volunteerhub.local', '+1 555-010-1005'
WHERE NOT EXISTS (SELECT 1 FROM volunteers WHERE email = 'demo.taylor@volunteerhub.local');

INSERT INTO volunteers (name, email, phone)
SELECT 'Riley Chen', 'demo.riley@volunteerhub.local', '+1 555-010-1006'
WHERE NOT EXISTS (SELECT 1 FROM volunteers WHERE email = 'demo.riley@volunteerhub.local');

INSERT INTO volunteers (name, email, phone)
SELECT 'Sam Rivera', 'demo.sam@volunteerhub.local', '+1 555-010-1007'
WHERE NOT EXISTS (SELECT 1 FROM volunteers WHERE email = 'demo.sam@volunteerhub.local');

INSERT INTO events (name, description, `date`, location, volunteer_capacity)
SELECT 'Community Meal Prep', 'Prepare and pack warm meals for local families.', '2026-11-05', 'Eastside Community Kitchen', 14
WHERE NOT EXISTS (SELECT 1 FROM events WHERE name = 'Community Meal Prep' AND `date` = '2026-11-05' AND location = 'Eastside Community Kitchen');

INSERT INTO events (name, description, `date`, location, volunteer_capacity)
SELECT 'Neighborhood Supply Fair', 'Organize and distribute school and winter supplies.', '2026-11-12', 'Riverside Recreation Center', 18
WHERE NOT EXISTS (SELECT 1 FROM events WHERE name = 'Neighborhood Supply Fair' AND `date` = '2026-11-12' AND location = 'Riverside Recreation Center');

INSERT INTO events (name, description, `date`, location, volunteer_capacity)
SELECT 'Native Garden Workshop', 'Build garden beds and plant native flowers.', '2026-11-19', 'Maple Street Garden', 10
WHERE NOT EXISTS (SELECT 1 FROM events WHERE name = 'Native Garden Workshop' AND `date` = '2026-11-19' AND location = 'Maple Street Garden');

INSERT INTO signups (event_id, volunteer_id, signup_date)
SELECT e.id, v.id, CURRENT_DATE FROM events e JOIN volunteers v ON v.email = 'demo.morgan@volunteerhub.local'
WHERE e.name = 'Community Meal Prep' AND e.`date` = '2026-11-05' AND NOT EXISTS (SELECT 1 FROM signups s WHERE s.event_id=e.id AND s.volunteer_id=v.id);

INSERT INTO signups (event_id, volunteer_id, signup_date)
SELECT e.id, v.id, CURRENT_DATE FROM events e JOIN volunteers v ON v.email = 'demo.taylor@volunteerhub.local'
WHERE e.name = 'Community Meal Prep' AND e.`date` = '2026-11-05' AND NOT EXISTS (SELECT 1 FROM signups s WHERE s.event_id=e.id AND s.volunteer_id=v.id);

INSERT INTO signups (event_id, volunteer_id, signup_date)
SELECT e.id, v.id, CURRENT_DATE FROM events e JOIN volunteers v ON v.email = 'demo.riley@volunteerhub.local'
WHERE e.name = 'Neighborhood Supply Fair' AND e.`date` = '2026-11-12' AND NOT EXISTS (SELECT 1 FROM signups s WHERE s.event_id=e.id AND s.volunteer_id=v.id);

INSERT INTO signups (event_id, volunteer_id, signup_date)
SELECT e.id, v.id, CURRENT_DATE FROM events e JOIN volunteers v ON v.email = 'demo.sam@volunteerhub.local'
WHERE e.name = 'Neighborhood Supply Fair' AND e.`date` = '2026-11-12' AND NOT EXISTS (SELECT 1 FROM signups s WHERE s.event_id=e.id AND s.volunteer_id=v.id);

INSERT INTO signups (event_id, volunteer_id, signup_date)
SELECT e.id, v.id, CURRENT_DATE FROM events e JOIN volunteers v ON v.email = 'demo.morgan@volunteerhub.local'
WHERE e.name = 'Native Garden Workshop' AND e.`date` = '2026-11-19' AND NOT EXISTS (SELECT 1 FROM signups s WHERE s.event_id=e.id AND s.volunteer_id=v.id);

INSERT INTO signups (event_id, volunteer_id, signup_date)
SELECT e.id, v.id, CURRENT_DATE FROM events e JOIN volunteers v ON v.email = 'demo.riley@volunteerhub.local'
WHERE e.name = 'Native Garden Workshop' AND e.`date` = '2026-11-19' AND NOT EXISTS (SELECT 1 FROM signups s WHERE s.event_id=e.id AND s.volunteer_id=v.id);

COMMIT;
