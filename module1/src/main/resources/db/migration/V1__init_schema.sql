-- ...existing code...
CREATE TABLE  product (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  price DOUBLE PRECISION,
  description TEXT,
  image_url VARCHAR(1024),
  quantity INTEGER,
  category VARCHAR(255)
);
