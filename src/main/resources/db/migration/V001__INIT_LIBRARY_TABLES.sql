-- Create extension for UUID support
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Books table
CREATE TABLE books
(
  id               UUID PRIMARY KEY         DEFAULT uuid_generate_v4(),
  isbn             VARCHAR(20)  NOT NULL UNIQUE,
  title            VARCHAR(255) NOT NULL,
  author           VARCHAR(255) NOT NULL,
  publisher        VARCHAR(255),
  publication_date DATE,
  description      TEXT,
  cover_image_url  VARCHAR(500),
  page_count       INTEGER,
  language         VARCHAR(50),
  category         VARCHAR(100),
  available        BOOLEAN                  DEFAULT TRUE,
  created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Library locations table
CREATE TABLE library_locations
(
  id            UUID PRIMARY KEY         DEFAULT uuid_generate_v4(),
  name          VARCHAR(255) NOT NULL,
  address       TEXT         NOT NULL,
  phone         VARCHAR(50),
  email         VARCHAR(255),
  opening_hours TEXT,
  created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create users table (supplemental to Keycloak)
CREATE TABLE library_users
(
  id            UUID PRIMARY KEY         DEFAULT uuid_generate_v4(),
  keycloak_id   VARCHAR(255) NOT NULL UNIQUE,
  first_name    VARCHAR(100) NOT NULL,
  last_name     VARCHAR(100) NOT NULL,
  email         VARCHAR(255) NOT NULL UNIQUE,
  phone         VARCHAR(50),
  address       TEXT,
  registered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  is_admin      BOOLEAN                  DEFAULT FALSE
);

-- Book loans table
CREATE TABLE book_loans
(
  id                 UUID PRIMARY KEY                  DEFAULT uuid_generate_v4(),
  book_id            UUID                     NOT NULL REFERENCES books (id),
  user_id            UUID                     NOT NULL REFERENCES library_users (id),
  pickup_location_id UUID REFERENCES library_locations (id),
  loan_date          TIMESTAMP WITH TIME ZONE          DEFAULT CURRENT_TIMESTAMP,
  due_date           TIMESTAMP WITH TIME ZONE NOT NULL,
  return_date        TIMESTAMP WITH TIME ZONE,
  status             VARCHAR(20)              NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, RETURNED, OVERDUE, RESERVED
  created_at         TIMESTAMP WITH TIME ZONE          DEFAULT CURRENT_TIMESTAMP,
  updated_at         TIMESTAMP WITH TIME ZONE          DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT loan_status_check CHECK (status IN ('ACTIVE', 'RETURNED', 'OVERDUE', 'RESERVED'))
);

-- Notifications table for tracking sent notifications
CREATE TABLE notifications
(
  id      UUID PRIMARY KEY         DEFAULT uuid_generate_v4(),
  loan_id UUID         NOT NULL REFERENCES book_loans (id),
  type    VARCHAR(50)  NOT NULL,                     -- EMAIL, SMS, etc.
  subject VARCHAR(255) NOT NULL,
  content TEXT         NOT NULL,
  sent_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  status  VARCHAR(20)  NOT NULL    DEFAULT 'QUEUED', -- QUEUED, SENT, FAILED

  CONSTRAINT notification_status_check CHECK (status IN ('QUEUED', 'SENT', 'FAILED'))
);

-- Book reviews table
CREATE TABLE book_reviews
(
  id         UUID PRIMARY KEY         DEFAULT uuid_generate_v4(),
  book_id    UUID    NOT NULL REFERENCES books (id),
  user_id    UUID    NOT NULL REFERENCES library_users (id),
  rating     INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
  comment    TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT unique_book_review UNIQUE (book_id, user_id)
);

-- Create indexes for better performance
CREATE INDEX idx_books_isbn ON books (isbn);
CREATE INDEX idx_books_title ON books (title);
CREATE INDEX idx_books_author ON books (author);
CREATE INDEX idx_books_category ON books (category);
CREATE INDEX idx_book_loans_book_id ON book_loans (book_id);
CREATE INDEX idx_book_loans_user_id ON book_loans (user_id);
CREATE INDEX idx_book_loans_status ON book_loans (status);
CREATE INDEX idx_book_loans_due_date ON book_loans (due_date);
CREATE INDEX idx_notifications_loan_id ON notifications (loan_id);
CREATE INDEX idx_book_reviews_book_id ON book_reviews (book_id);

-- Add trigger to update 'updated_at' timestamp
CREATE OR REPLACE FUNCTION update_modified_column()
  RETURNS TRIGGER AS
$$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_books_modtime
  BEFORE UPDATE
  ON books
  FOR EACH ROW
EXECUTE PROCEDURE update_modified_column();

CREATE TRIGGER update_book_loans_modtime
  BEFORE UPDATE
  ON book_loans
  FOR EACH ROW
EXECUTE PROCEDURE update_modified_column();
