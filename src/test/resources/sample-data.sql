-- Insert library locations
INSERT INTO library_locations (id, name, address, phone, email, opening_hours)
VALUES
  (uuid_generate_v4(), 'Main Library', '123 Main St, Springfield, IL 62701', '(217) 555-1234', 'main@mockshelf.com', 'Mon-Fri: 9am-8pm, Sat-Sun: 10am-6pm'),
  (uuid_generate_v4(), 'North Branch', '456 North Ave, Springfield, IL 62702', '(217) 555-5678', 'north@mockshelf.com', 'Mon-Fri: 10am-7pm, Sat: 10am-5pm, Sun: Closed'),
  (uuid_generate_v4(), 'South Branch', '789 South Blvd, Springfield, IL 62703', '(217) 555-9012', 'south@mockshelf.com', 'Mon-Thurs: 10am-7pm, Fri-Sat: 10am-5pm, Sun: Closed'),
  (uuid_generate_v4(), 'East Branch', '321 East Rd, Springfield, IL 62704', '(217) 555-3456', 'east@mockshelf.com', 'Tue-Sat: 10am-6pm, Sun-Mon: Closed');

-- Insert sample books
INSERT INTO books (id, isbn, title, author, publisher, publication_date, description, cover_image_url, page_count, language, category, available)
VALUES
  (uuid_generate_v4(), '9780132350884', 'Clean Code', 'Robert C. Martin', 'Prentice Hall', '2008-08-01', 'A handbook of agile software craftsmanship', 'https://images-na.ssl-images-amazon.com/images/I/41xShlnTZTL._SX376_BO1,204,203,200_.jpg', 464, 'English', 'Programming', true),

  (uuid_generate_v4(), '9780134494166', 'Clean Architecture', 'Robert C. Martin', 'Prentice Hall', '2017-09-10', 'A Craftsman''s Guide to Software Structure and Design', 'https://images-na.ssl-images-amazon.com/images/I/41-sN-mzwKL._SX380_BO1,204,203,200_.jpg', 432, 'English', 'Programming', true),

  (uuid_generate_v4(), '9780321125217', 'Domain-Driven Design', 'Eric Evans', 'Addison-Wesley', '2003-08-30', 'Tackling Complexity in the Heart of Software', 'https://images-na.ssl-images-amazon.com/images/I/51sZW87slRL._SX375_BO1,204,203,200_.jpg', 560, 'English', 'Programming', true),

  (uuid_generate_v4(), '9781617294549', 'Spring Boot in Action', 'Craig Walls', 'Manning Publications', '2015-12-28', 'A developer-focused guide to Spring Boot', 'https://images.manning.com/book/0/a9f93bc-7004-4797-bcd2-7639288b3eac/Walls-SpringBoot-HI.png', 264, 'English', 'Programming', false),

  (uuid_generate_v4(), '9781492056614', 'Spring Security in Action', 'Laurentiu Spilca', 'Manning Publications', '2020-09-29', 'Comprehensive guide to securing your Spring applications', 'https://images.manning.com/book/b/24df0da-f94c-481b-bcc1-8a7e5421ab7a/Spilca-SpringSiA-HI.png', 560, 'English', 'Programming', true),

  (uuid_generate_v4(), '9781789348811', 'Testing Java Microservices', 'Alex Soto Bueno, Andy Gumbrecht, Jason Porter', 'Manning Publications', '2018-03-07', 'Using JUnit 5, Mockito, and Spring Boot', 'https://images.manning.com/book/2/0e33573-a025-46e4-a3bf-b2aa7bdf9c25/Soto-Testing-HI.png', 350, 'English', 'Programming', true),

  (uuid_generate_v4(), '9781449373320', 'Node.js Design Patterns', 'Mario Casciaro', 'O''Reilly Media', '2014-12-25', 'Design and implement Node.js applications', 'https://images-na.ssl-images-amazon.com/images/I/51qQTzOqgIL._SX379_BO1,204,203,200_.jpg', 312, 'English', 'Programming', true),

  (uuid_generate_v4(), '9780596007126', 'Head First Design Patterns', 'Eric Freeman, Elisabeth Robson, Bert Bates, Kathy Sierra', 'O''Reilly Media', '2004-10-01', 'A Brain-Friendly Guide to Design Patterns', 'https://images-na.ssl-images-amazon.com/images/I/51szD9HC9pL._SX430_BO1,204,203,200_.jpg', 694, 'English', 'Programming', true),

  (uuid_generate_v4(), '9781491950357', 'Building Microservices', 'Sam Newman', 'O''Reilly Media', '2015-02-02', 'Designing Fine-Grained Systems', 'https://images-na.ssl-images-amazon.com/images/I/51m85J4Zi9L._SX379_BO1,204,203,200_.jpg', 280, 'English', 'Programming', true),

  (uuid_generate_v4(), '9781617292545', 'Spring in Action', 'Craig Walls', 'Manning Publications', '2018-10-05', 'Covers Spring 5.0', 'https://images.manning.com/book/f/cd76c65-856f-4b8f-b7fc-4b62cd46a852/Walls-Spring-5ed-HI.png', 520, 'English', 'Programming', true);

-- Insert sample users (for test environment, in production these would be linked to Keycloak)
INSERT INTO library_users (id, keycloak_id, first_name, last_name, email, phone, address, is_admin)
VALUES
  (uuid_generate_v4(), 'admin-user-1', 'Admin', 'User', 'admin@mockshelf.com', '(555) 123-4567', '123 Admin St, Springfield, IL 62701', true),

  (uuid_generate_v4(), 'regular-user-1', 'John', 'Doe', 'john.doe@example.com', '(555) 234-5678', '456 User Ave, Springfield, IL 62702', false),

  (uuid_generate_v4(), 'regular-user-2', 'Jane', 'Smith', 'jane.smith@example.com', '(555) 345-6789', '789 Member Ln, Springfield, IL 62703', false),

  (uuid_generate_v4(), 'regular-user-3', 'Bob', 'Johnson', 'bob.johnson@example.com', '(555) 456-7890', '321 Reader Rd, Springfield, IL 62704', false),

  (uuid_generate_v4(), 'regular-user-4', 'Alice', 'Williams', 'alice.williams@example.com', '(555) 567-8901', '654 Borrower Blvd, Springfield, IL 62705', false);

-- Get IDs for reference in book loans
DO $$
DECLARE
spring_boot_id UUID;
    john_doe_id UUID;
    jane_smith_id UUID;
    main_library_id UUID;
    north_branch_id UUID;
BEGIN
    -- Get book IDs
SELECT id INTO spring_boot_id FROM books WHERE isbn = '9781617294549';

-- Get user IDs
SELECT id INTO john_doe_id FROM library_users WHERE email = 'john.doe@example.com';
SELECT id INTO jane_smith_id FROM library_users WHERE email = 'jane.smith@example.com';

-- Get location IDs
SELECT id INTO main_library_id FROM library_locations WHERE name = 'Main Library';
SELECT id INTO north_branch_id FROM library_locations WHERE name = 'North Branch';

-- Insert sample book loans
INSERT INTO book_loans (id, book_id, user_id, pickup_location_id, loan_date, due_date, status)
VALUES
  (uuid_generate_v4(), spring_boot_id, john_doe_id, main_library_id, CURRENT_TIMESTAMP - INTERVAL '14 days', CURRENT_TIMESTAMP + INTERVAL '7 days', 'ACTIVE'),

  (uuid_generate_v4(), (SELECT id FROM books WHERE isbn = '9780321125217'), jane_smith_id, north_branch_id, CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '1 day', 'OVERDUE');

-- Insert sample book reviews
INSERT INTO book_reviews (id, book_id, user_id, rating, comment)
VALUES
  (uuid_generate_v4(), (SELECT id FROM books WHERE isbn = '9780132350884'), john_doe_id, 5, 'An essential read for every serious developer!'),

  (uuid_generate_v4(), spring_boot_id, jane_smith_id, 4, 'Great introduction to Spring Boot, helped me get started quickly.');
END $$;
