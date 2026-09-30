CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL, -- уникальность обеспечивается индексом по LOWER(name) ниже
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Индекс для проверки уникальности имени категории без учета регистра. Также ускоряет поиск по имени
CREATE UNIQUE INDEX idx_categories_name_lower ON categories (LOWER(name));
