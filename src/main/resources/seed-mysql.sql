-- ==============================================================================
-- Campus Placement Management System - Seed Data (MySQL 8+)
-- ==============================================================================

INSERT INTO companies (id, name, industry, website, contact_person, contact_email, hr_phone) VALUES
(1, 'Tata Consultancy Services', 'IT Services', 'https://www.tcs.com', 'Rohan Mehta', 'rohan.mehta@tcs.com', '9876501001'),
(2, 'Infosys Limited', 'IT Services', 'https://www.infosys.com', 'Priya Nair', 'priya.nair@infosys.com', '9876501002'),
(3, 'Microsoft India', 'Product & Cloud', 'https://www.microsoft.com', 'Ananya Verma', 'ananya.v@microsoft.com', '9876501003'),
(4, 'Amazon Development Centre', 'E-Commerce & Cloud', 'https://www.amazon.jobs', 'Vikram Kapoor', 'vkapoor@amazon.com', '9876501004'),
(5, 'Larsen & Toubro', 'Core Engineering', 'https://www.larsentoubro.com', 'Suresh Iyer', 'suresh.iyer@lnt.com', '9876501005'),
(6, 'Bosch Global Software', 'Automotive & Embedded', 'https://www.bosch.in', 'Neha Kulkarni', 'neha.k@bosch.com', '9876501006'),
(7, 'Deloitte USI', 'Consulting & Analytics', 'https://www2.deloitte.com', 'Karan Malhotra', 'kmalhotra@deloitte.com', '9876501007'),
(8, 'Reliance Industries', 'Energy & Petrochemicals', 'https://www.ril.com', 'Deepak Joshi', 'deepak.j@ril.com', '9876501008');

INSERT INTO users (id, email, password_hash, role, company_id, active) VALUES
(1, 'admin@campus.edu', '65536:Y2FtcHVzX2FkbWluX3NhbHQ=:pQZzkBIZOCQhzKkqZKYOTWqNrHEowqjYWPeOOtjBHTU=', 'ADMIN', NULL, 1),
(2, 'recruiter.tcs@campus.edu', '65536:Y2FtcHVzX3JlY19zYWx0XzE=:rvY3Fn8OU0yXGH1FnPSVZ91Pc3KWHwvrn5FsO1+EoHg=', 'RECRUITER', 1, 1),
(3, 'recruiter.infosys@campus.edu', '65536:Y2FtcHVzX3JlY19zYWx0XzE=:rvY3Fn8OU0yXGH1FnPSVZ91Pc3KWHwvrn5FsO1+EoHg=', 'RECRUITER', 2, 1),
(4, 'aarav.sharma@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(5, 'diya.patel@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(6, 'kabir.singh@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(7, 'meera.reddy@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(8, 'aditya.nair@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(9, 'ishita.gupta@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(10, 'rohit.kumar@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(11, 'sneha.joshi@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(12, 'arjun.menon@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(13, 'tanvi.desai@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(14, 'varun.chopra@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(15, 'pooja.bhat@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(16, 'nikhil.rao@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(17, 'anika.jain@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(18, 'siddharth.roy@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(19, 'kavya.iyer@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(20, 'manav.bansal@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(21, 'riya.kapoor@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(22, 'harsh.agarwal@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(23, 'nisha.tiwari@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(24, 'pranav.kulkarni@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(25, 'shreya.saxena@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(26, 'yash.mishra@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(27, 'simran.kaur@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(28, 'dev.malhotra@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1),
(29, 'tara.bose@campus.edu', '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=', 'STUDENT', NULL, 1);

INSERT INTO students (id, user_id, roll_no, full_name, email, phone, branch, batch_year, cgpa, active_backlogs, skills, blocked) VALUES
(1,  4,  '102203001', 'Aarav Sharma',    'aarav.sharma@campus.edu',    '9811100001', 'CSE', 2026, 9.15, 0, 'Java, SQL, JavaFX, DSA', 0),
(2,  5,  '102203002', 'Diya Patel',      'diya.patel@campus.edu',      '9811100002', 'CSE', 2026, 8.75, 0, 'Python, Machine Learning, SQL', 0),
(3,  6,  '102203003', 'Kabir Singh',     'kabir.singh@campus.edu',     '9811100003', 'CSE', 2026, 7.90, 0, 'Java, React, Node.js', 0),
(4,  7,  '102203004', 'Meera Reddy',     'meera.reddy@campus.edu',     '9811100004', 'CSE', 2026, 9.45, 0, 'C++, Distributed Systems, Cloud', 0),
(5,  8,  '102203005', 'Aditya Nair',     'aditya.nair@campus.edu',     '9811100005', 'CSE', 2026, 6.80, 1, 'Java, HTML, CSS', 0),
(6,  9,  '102203006', 'Ishita Gupta',    'ishita.gupta@campus.edu',    '9811100006', 'CSE', 2026, 8.20, 0, 'Java, JDBC, MySQL, Git', 0),
(7,  10, '102204001', 'Rohit Kumar',     'rohit.kumar@campus.edu',     '9811100007', 'ECE', 2026, 8.40, 0, 'Embedded C, VLSI, Verilog', 0),
(8,  11, '102204002', 'Sneha Joshi',     'sneha.joshi@campus.edu',     '9811100008', 'ECE', 2026, 7.65, 0, 'Signal Processing, MATLAB, Python', 0),
(9,  12, '102204003', 'Arjun Menon',     'arjun.menon@campus.edu',     '9811100009', 'ECE', 2026, 9.05, 0, 'IoT, Embedded Linux, C++', 0),
(10, 13, '102204004', 'Tanvi Desai',     'tanvi.desai@campus.edu',     '9811100010', 'ECE', 2026, 6.45, 2, 'PCB Design, Arduino', 0),
(11, 14, '102204005', 'Varun Chopra',    'varun.chopra@campus.edu',    '9811100011', 'ECE', 2026, 8.10, 0, 'Java, DSA, Networking', 0),
(12, 15, '102205001', 'Pooja Bhat',      'pooja.bhat@campus.edu',      '9811100012', 'ME',  2026, 8.55, 0, 'AutoCAD, SolidWorks, ANSYS', 0),
(13, 16, '102205002', 'Nikhil Rao',      'nikhil.rao@campus.edu',      '9811100013', 'ME',  2026, 7.30, 0, 'Thermodynamics, CATIA, Python', 0),
(14, 17, '102205003', 'Anika Jain',      'anika.jain@campus.edu',      '9811100014', 'ME',  2026, 8.85, 0, 'Robotics, MATLAB, SolidWorks', 0),
(15, 18, '102205004', 'Siddharth Roy',   'siddharth.roy@campus.edu',   '9811100015', 'ME',  2026, 5.95, 1, 'CAD, Manufacturing', 0),
(16, 19, '102205005', 'Kavya Iyer',      'kavya.iyer@campus.edu',      '9811100016', 'ME',  2026, 7.80, 0, 'Supply Chain, Excel, SQL', 0),
(17, 20, '102206001', 'Manav Bansal',    'manav.bansal@campus.edu',    '9811100017', 'EE',  2026, 8.35, 0, 'Power Systems, PLC, MATLAB', 0),
(18, 21, '102206002', 'Riya Kapoor',     'riya.kapoor@campus.edu',     '9811100018', 'EE',  2026, 7.50, 0, 'Control Systems, Simulink, C', 0),
(19, 22, '102206003', 'Harsh Agarwal',   'harsh.agarwal@campus.edu',   '9811100019', 'EE',  2026, 8.95, 0, 'EV Powertrain, Embedded C, Python', 0),
(20, 23, '102206004', 'Nisha Tiwari',    'nisha.tiwari@campus.edu',    '9811100020', 'EE',  2026, 6.90, 0, 'Switchgear, AutoCAD Electrical', 0),
(21, 24, '102206005', 'Pranav Kulkarni', 'pranav.kulkarni@campus.edu', '9811100021', 'EE',  2026, 7.15, 1, 'Java, SQL, Power Electronics', 0),
(22, 25, '102207001', 'Shreya Saxena',   'shreya.saxena@campus.edu',   '9811100022', 'CHE', 2026, 8.60, 0, 'Aspen Plus, Process Design, Python', 0),
(23, 26, '102207002', 'Yash Mishra',     'yash.mishra@campus.edu',     '9811100023', 'CHE', 2026, 7.45, 0, 'Fluid Mechanics, HYSYS, Excel', 0),
(24, 27, '102207003', 'Simran Kaur',     'simran.kaur@campus.edu',     '9811100024', 'CHE', 2026, 9.10, 0, 'Reaction Engineering, Data Analytics', 0),
(25, 28, '102207004', 'Dev Malhotra',    'dev.malhotra@campus.edu',    '9811100025', 'CHE', 2026, 6.20, 0, 'Safety Engineering, CAD', 1),
(26, 29, '102203007', 'Tara Bose',       'tara.bose@campus.edu',       '9811100026', 'CSE', 2025, 8.50, 0, 'Java, Cloud, Docker', 0);

INSERT INTO drives (id, company_id, job_role, package_lpa, location, job_type, drive_date, last_date, min_cgpa, max_backlogs, batch_year, status, description) VALUES
(1,  1, 'Assistant System Engineer',    7.00,  'Noida / Pune',        'FULL_TIME',  '2026-12-15', '2026-12-10', 6.50, 1, 2026, 'OPEN',      'Enterprise Java & Cloud delivery role.'),
(2,  1, 'TCS Digital Developer',        9.50,  'Bengaluru',           'FULL_TIME',  '2026-12-20', '2026-12-15', 7.50, 0, 2026, 'OPEN',      'Full-stack & data engineering track.'),
(3,  2, 'Systems Engineer',             6.25,  'Mysuru / Hyderabad',  'FULL_TIME',  '2026-12-18', '2026-12-12', 6.00, 1, 2026, 'OPEN',      'Core software engineering & testing.'),
(4,  2, 'Specialist Programmer',        9.50,  'Bengaluru',           'FULL_TIME',  '2026-12-28', '2026-12-22', 8.00, 0, 2026, 'OPEN',      'Competitive coding & product engineering.'),
(5,  3, 'Software Engineer (SDE-1)',    42.00, 'Hyderabad',           'FULL_TIME',  '2026-12-22', '2026-12-14', 8.50, 0, 2026, 'OPEN',      'Azure & platform engineering.'),
(6,  3, 'SWE Intern + PPO',             18.00, 'Bengaluru',           'INTERN_PPO', '2027-01-10', '2026-12-30', 8.00, 0, 2026, 'UPCOMING',  '6-month internship with PPO conversion.'),
(7,  4, 'SDE-1 (AWS)',                  36.00, 'Bengaluru',           'FULL_TIME',  '2026-12-19', '2026-12-12', 8.00, 0, 2026, 'OPEN',      'Backend distributed systems at AWS.'),
(8,  5, 'Graduate Engineer Trainee',    8.50,  'Mumbai / Chennai',    'FULL_TIME',  '2026-12-24', '2026-12-18', 7.00, 0, 2026, 'OPEN',      'Core infrastructure & heavy engineering.'),
(9,  6, 'Embedded Systems Engineer',    11.00, 'Bengaluru / Pune',    'FULL_TIME',  '2026-12-27', '2026-12-20', 7.50, 0, 2026, 'OPEN',      'Automotive ECU & embedded C++.'),
(10, 7, 'Technology Analyst',           10.50, 'Hyderabad / Gurugram','FULL_TIME',  '2026-11-10', '2026-11-05', 7.00, 0, 2026, 'COMPLETED', 'Enterprise consulting & analytics.'),
(11, 8, 'Graduate Engineer Trainee',    9.00,  'Jamnagar / Mumbai',   'FULL_TIME',  '2026-12-29', '2026-12-21', 7.00, 0, 2026, 'OPEN',      'Process & plant engineering.'),
(12, 4, 'Cloud Support Intern',         12.00, 'Hyderabad',           'INTERNSHIP', '2026-11-20', '2026-11-15', 7.00, 1, 2026, 'CLOSED',    'Summer/Winter cloud support internship.');

INSERT INTO drive_branches (drive_id, branch) VALUES
(1, 'CSE'), (1, 'ECE'), (1, 'EE'),
(2, 'CSE'), (2, 'ECE'),
(3, 'CSE'), (3, 'ECE'), (3, 'ME'), (3, 'EE'), (3, 'CHE'),
(4, 'CSE'), (4, 'ECE'),
(5, 'CSE'),
(6, 'CSE'), (6, 'ECE'),
(7, 'CSE'), (7, 'ECE'),
(8, 'ME'),  (8, 'EE'),  (8, 'CHE'),
(9, 'ECE'), (9, 'EE'),  (9, 'CSE'),
(10, 'CSE'), (10, 'ECE'), (10, 'ME'),
(11, 'CHE'), (11, 'ME'),  (11, 'EE'),
(12, 'CSE'), (12, 'ECE');

INSERT INTO applications (id, student_id, drive_id, status) VALUES
(1,  1, 10, 'SELECTED'),
(2,  2, 10, 'SELECTED'),
(3,  3, 10, 'REJECTED'),
(4,  1, 5,  'INTERVIEW'),
(5,  4, 5,  'SHORTLISTED'),
(6,  4, 7,  'TEST'),
(7,  6, 1,  'APPLIED'),
(8,  7, 9,  'SHORTLISTED'),
(9,  9, 9,  'INTERVIEW'),
(10, 12, 8, 'SELECTED'),
(11, 14, 8, 'SHORTLISTED'),
(12, 17, 8, 'APPLIED'),
(13, 22, 11, 'APPLIED'),
(14, 24, 11, 'SHORTLISTED');

INSERT INTO application_status_history (application_id, from_status, to_status, changed_by_user_id, remarks) VALUES
(1, NULL, 'APPLIED', 4, 'Student applied'),
(1, 'APPLIED', 'SHORTLISTED', 1, 'Cleared CGPA screen'),
(1, 'SHORTLISTED', 'TEST', 1, 'Cleared online test'),
(1, 'TEST', 'INTERVIEW', 1, 'Cleared technical round'),
(1, 'INTERVIEW', 'SELECTED', 1, 'Final selection confirmed'),
(2, NULL, 'APPLIED', 5, 'Student applied'),
(2, 'APPLIED', 'SHORTLISTED', 1, 'Shortlisted'),
(2, 'SHORTLISTED', 'TEST', 1, 'Test cleared'),
(2, 'TEST', 'INTERVIEW', 1, 'Interview cleared'),
(2, 'INTERVIEW', 'SELECTED', 1, 'Selected for Technology Analyst'),
(3, NULL, 'APPLIED', 6, 'Student applied'),
(3, 'APPLIED', 'REJECTED', 1, 'Did not meet test cutoff'),
(4, NULL, 'APPLIED', 4, 'Student applied'),
(4, 'APPLIED', 'SHORTLISTED', 1, 'Shortlisted by Microsoft'),
(4, 'SHORTLISTED', 'TEST', 1, 'Passed coding test'),
(4, 'TEST', 'INTERVIEW', 1, 'Scheduled for final interview'),
(10, NULL, 'APPLIED', 15, 'Student applied'),
(10, 'APPLIED', 'SHORTLISTED', 1, 'Shortlisted'),
(10, 'SHORTLISTED', 'TEST', 1, 'Test cleared'),
(10, 'TEST', 'INTERVIEW', 1, 'Interview cleared'),
(10, 'INTERVIEW', 'SELECTED', 1, 'Selected at L&T');

INSERT INTO offers (id, application_id, student_id, drive_id, package_lpa, joining_date, status, responded_at) VALUES
(1, 1,  1,  10, 10.50, '2026-07-01', 'ACCEPTED', NOW()),
(2, 2,  2,  10, 10.50, '2026-07-01', 'PENDING',  NULL),
(3, 10, 12, 8,  8.50,  '2026-07-15', 'ACCEPTED', NOW());

INSERT INTO notifications (user_id, title, message, is_read) VALUES
(4, 'Offer Issued: Deloitte USI', 'Congratulations! You received an offer for Technology Analyst (10.50 LPA).', 1),
(5, 'Offer Issued: Deloitte USI', 'Congratulations! You received an offer for Technology Analyst (10.50 LPA). Please respond.', 0),
(15, 'Offer Issued: Larsen & Toubro', 'Congratulations! You received an offer for Graduate Engineer Trainee (8.50 LPA).', 1);

INSERT INTO audit_log (actor_user_id, action, entity_type, entity_id, details) VALUES
(1, 'SEED_INIT', 'SYSTEM', NULL, 'Initial seed data loaded');

-- =========================================================
-- ADDITIONAL SHOWCASE STUDENT ACCOUNTS
-- Run AFTER your original seed-mysql.sql
-- =========================================================

INSERT INTO users (id, email, password_hash, role, company_id, active) VALUES
(30, 'naina.verma@campus.edu',
 '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=',
 'STUDENT', NULL, 1),
(31, 'raghav.malhotra@campus.edu',
 '65536:Y2FtcHVzX3N0dV9zYWx0XzE=:U0zFNS5PUYbT4cP0ClKDZsQ3RA0jErMfEevNj0AmPTg=',
 'STUDENT', NULL, 1);

INSERT INTO students
(id, user_id, roll_no, full_name, email, phone, branch, batch_year, cgpa, active_backlogs, skills, blocked)
VALUES
(27, 30, '102203008', 'Naina Verma',
 'naina.verma@campus.edu', '9811100027',
 'CSE', 2026, 9.20, 0,
 'Java, Python, DSA, Machine Learning, SQL, Git', 0),

(28, 31, '102204006', 'Raghav Malhotra',
 'raghav.malhotra@campus.edu', '9811100028',
 'ECE', 2026, 8.60, 0,
 'C++, Embedded C, IoT, Python, MATLAB, Verilog', 0);

-- =========================================================
-- ADDITIONAL PLACEMENT DRIVES
-- =========================================================

INSERT INTO drives
(id, company_id, job_role, package_lpa, location, job_type,
 drive_date, last_date, min_cgpa, max_backlogs, batch_year, status, description)
VALUES
(13, 3, 'Software Engineer - Product', 42.00, 'Hyderabad',
 'FULL_TIME', '2026-12-18', '2026-12-10',
 8.50, 0, 2026, 'OPEN',
 'Product development, cloud platforms and software engineering.'),

(14, 4, 'Software Development Engineer', 36.00, 'Bengaluru',
 'FULL_TIME', '2026-12-20', '2026-12-12',
 8.00, 0, 2026, 'OPEN',
 'Backend development, problem solving and distributed systems.'),

(15, 2, 'Specialist Programmer', 9.50, 'Bengaluru',
 'FULL_TIME', '2026-12-21', '2026-12-15',
 7.80, 0, 2026, 'OPEN',
 'Software engineering, programming and application development.');

INSERT INTO drive_branches (drive_id, branch) VALUES
(13, 'CSE'),
(14, 'CSE'),
(14, 'ECE'),
(15, 'CSE'),
(15, 'ECE'),
(15, 'ME'),
(15, 'EE'),
(15, 'CHE');

-- =========================================================
-- ADDITIONAL APPLICATIONS
-- =========================================================

INSERT INTO applications (id, student_id, drive_id, status) VALUES
(15, 27, 13, 'SELECTED'),
(16, 27, 14, 'INTERVIEW'),
(17, 28, 14, 'SELECTED'),
(18, 28, 15, 'APPLIED');

-- =========================================================
-- APPLICATION STATUS HISTORY
-- =========================================================

INSERT INTO application_status_history
(application_id, from_status, to_status, changed_by_user_id, remarks)
VALUES
(15, NULL, 'APPLIED', 30, 'Naina applied for the Microsoft drive'),
(15, 'APPLIED', 'SHORTLISTED', 1, 'CGPA and branch eligibility verified'),
(15, 'SHORTLISTED', 'TEST', 1, 'Cleared coding assessment'),
(15, 'TEST', 'INTERVIEW', 1, 'Technical interview cleared'),
(15, 'INTERVIEW', 'SELECTED', 1, 'Final selection'),

(16, NULL, 'APPLIED', 30, 'Naina applied for Amazon SDE'),
(16, 'APPLIED', 'SHORTLISTED', 1, 'Profile shortlisted'),
(16, 'SHORTLISTED', 'TEST', 1, 'Coding test cleared'),
(16, 'TEST', 'INTERVIEW', 1, 'Advanced to interview'),

(17, NULL, 'APPLIED', 31, 'Raghav applied for Amazon SDE'),
(17, 'APPLIED', 'SHORTLISTED', 1, 'Profile shortlisted'),
(17, 'SHORTLISTED', 'TEST', 1, 'Assessment cleared'),
(17, 'TEST', 'INTERVIEW', 1, 'Interview completed'),
(17, 'INTERVIEW', 'SELECTED', 1, 'Final selection');

-- =========================================================
-- ADDITIONAL OFFERS
-- =========================================================

INSERT INTO offers
(id, application_id, student_id, drive_id, package_lpa, joining_date, status, responded_at)
VALUES
(4, 15, 27, 13, 42.00, '2027-07-01', 'ACCEPTED', NOW()),
(5, 17, 28, 14, 36.00, '2027-07-01', 'PENDING', NULL);

-- =========================================================
-- ADDITIONAL STUDENT NOTIFICATIONS
-- =========================================================

INSERT INTO notifications (user_id, title, message, is_read) VALUES
(30, 'Congratulations! Microsoft Offer Received',
 'You have been selected for Software Engineer - Product at Microsoft with a 42 LPA package.',
 0),

(30, 'Amazon Interview Update',
 'Your Amazon SDE application has progressed to the interview stage.',
 0),

(31, 'Amazon Placement Update',
 'Congratulations! You have been selected for the Amazon Software Development Engineer role.',
 0),

(31, 'New Placement Drive',
 'Infosys Specialist Programmer drive is open. Check your eligibility and apply.',
 0);

-- =========================================================
-- AUDIT LOG
-- =========================================================

INSERT INTO audit_log
(actor_user_id, action, entity_type, entity_id, details)
VALUES
(1, 'DEMO_DATA_ADDED', 'STUDENT', 27, 'Added showcase student Naina Verma'),
(1, 'DEMO_DATA_ADDED', 'STUDENT', 28, 'Added showcase student Raghav Malhotra'),
(1, 'DEMO_DATA_ADDED', 'DRIVE', 13, 'Added Microsoft Product Software Engineer demo drive'),
(1, 'DEMO_DATA_ADDED', 'DRIVE', 14, 'Added Amazon SDE demo drive'),
(1, 'DEMO_DATA_ADDED', 'DRIVE', 15, 'Added Infosys Specialist Programmer demo drive');
