-- Initialize library locations
INSERT INTO library_locations (id, name, address, phone, email, opening_hours)
VALUES
  (gen_random_uuid(), 'Main Library', '123 Main Street, Springfield, IL 62701', '(217) 555-1234', 'main@mockshelf.com', 'Monday-Friday: 9am-8pm, Saturday: 10am-6pm, Sunday: 12pm-5pm'),
  (gen_random_uuid(), 'North Branch', '456 North Avenue, Springfield, IL 62702', '(217) 555-5678', 'north@mockshelf.com', 'Monday-Thursday: 10am-7pm, Friday-Saturday: 10am-5pm, Sunday: Closed'),
  (gen_random_uuid(), 'Tech Hub', '789 Innovation Drive, Springfield, IL 62703', '(217) 555-9012', 'tech@mockshelf.com', 'Monday-Friday: 11am-9pm, Saturday-Sunday: 10am-6pm'),
  (gen_random_uuid(), 'University Library', '101 College Road, Springfield, IL 62704', '(217) 555-3456', 'university@mockshelf.com', 'Monday-Friday: 8am-10pm, Saturday: 9am-7pm, Sunday: 10am-7pm');

-- Initialize programming books
INSERT INTO books (id, isbn, title, author, publisher, publication_date, description, cover_image_url, page_count, language, category, available)
VALUES
  (
    gen_random_uuid(),
    '9780132350884',
    'Clean Code: A Handbook of Agile Software Craftsmanship',
    'Robert C. Martin',
    'Prentice Hall',
    '2008-08-01',
    'Even bad code can function. But if code isn''t clean, it can bring a development organization to its knees. Every year, countless hours and significant resources are lost because of poorly written code. But it doesn''t have to be that way. Noted software expert Robert C. Martin presents a revolutionary paradigm with Clean Code: A Handbook of Agile Software Craftsmanship.',
    'https://m.media-amazon.com/images/I/51E2055ZGUL._SY445_SX342_.jpg',
    464,
    'English',
    'Programming',
    TRUE
  ),
  (
    gen_random_uuid(),
    '9780201633610',
    'Design Patterns: Elements of Reusable Object-Oriented Software',
    'Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides',
    'Addison-Wesley Professional',
    '1994-11-10',
    'Capturing a wealth of experience about the design of object-oriented software, four top-notch designers present a catalog of simple and succinct solutions to commonly occurring design problems. Previously undocumented, these 23 patterns allow designers to create more flexible, elegant, and ultimately reusable designs without having to rediscover the design solutions themselves.',
    'https://m.media-amazon.com/images/I/51szD9HC9pL._SY445_SX342_.jpg',
    416,
    'English',
    'Programming',
    TRUE
  ),
  (
    gen_random_uuid(),
    '9780134685991',
    'Effective Java',
    'Joshua Bloch',
    'Addison-Wesley Professional',
    '2018-01-06',
    'The Definitive Guide to Java Platform Best Practices–Updated for Java 7, 8, and 9. Since this Jolt-award winning classic was last updated in 2008, the Java programming environment has changed dramatically. Java 7 and Java 8 introduced new features and functions including, forEach() method in Iterable interface, default and static methods in Interfaces, Java Stream API, Java Time API, Collection API improvements and more.',
    'https://m.media-amazon.com/images/I/41zLisPNN2L._SY445_SX342_.jpg',
    412,
    'English',
    'Programming',
    TRUE
  ),
  (
    gen_random_uuid(),
    '9780135957059',
    'The Pragmatic Programmer: Your Journey to Mastery',
    'David Thomas, Andrew Hunt',
    'Addison-Wesley Professional',
    '2019-09-13',
    'The Pragmatic Programmer is one of those rare tech audiobooks you''ll listen, re-listen, and listen to again over the years. Whether you''re new to the field or an experienced practitioner, you''ll come away with fresh insights each and every time. Dave Thomas and Andy Hunt wrote the first edition of this influential book in 1999 to help their clients create better software and rediscover the joy of coding.',
    'https://m.media-amazon.com/images/I/51IA4hT6jrL._SY445_SX342_.jpg',
    352,
    'English',
    'Programming',
    TRUE
  ),
  (
    gen_random_uuid(),
    '9780262033848',
    'Introduction to Algorithms',
    'Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, Clifford Stein',
    'MIT Press',
    '2009-07-31',
    'Some books on algorithms are rigorous but incomplete; others cover masses of material but lack rigor. Introduction to Algorithms uniquely combines rigor and comprehensiveness. The book covers a broad range of algorithms in depth, yet makes their design and analysis accessible to all levels of readers.',
    'https://m.media-amazon.com/images/I/41T0iBxY8FL._SY445_SX342_.jpg',
    1312,
    'English',
    'Computer Science',
    TRUE
  );
