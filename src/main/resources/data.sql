-- Удаляем старые записи
DELETE FROM catalog_items;

-- Наполняем каталог полами

-- Кварцевый ламинат SPC
INSERT INTO catalog_items (category, name, description, photo_url) VALUES
                                                                       ('SPC Ламинат', 'SPC Fargo Herringbone Дуб Рокфорд', 'Каменный SPC ламинат с водостойкостью и высокой износостойкостью, класс 42. Подходит для квартиры и коммерческих помещений.', 'https://refloor-opt.ru/images/products/spc-fargo-herringbone-rokford.jpg'),
                                                                       ('SPC Ламинат', 'SPC Fargo Herringbone Дуб Верде', 'Кварцевый SPC ламинат с эстетичным рисунком дерева и повышенной прочностью.', 'https://refloor-opt.ru/images/products/spc-fargo-herringbone-verde.jpg'),
                                                                       ('SPC Ламинат', 'SPC Fargo Herringbone Дуб Борнео', 'SPC ламинат каменно‑полимерного типа, стойкий к влаге и износу, легко укладывается.', 'https://refloor-opt.ru/images/products/spc-fargo-herringbone-borneo.jpg');

-- Виниловые полы (LVT / Luxury Vinyl)
INSERT INTO catalog_items (category, name, description, photo_url) VALUES
                                                                       ('Виниловые Полы', 'Luxury Vinyl Washed Oak', 'Высококачественные виниловые полы с защитным слоем, устойчивы к изнашиванию, подходят для любых помещений.', 'https://refloor-opt.ru/images/products/luxury-vinyl-washed-oak.jpg'),
                                                                       ('Виниловые Полы', 'Luxury Vinyl Wood Drift', 'Экологичные виниловые полы с классическим дизайном древесной текстуры.', 'https://refloor-opt.ru/images/products/luxury-vinyl-wood-drift.jpg'),
                                                                       ('Виниловые Полы', 'Luxury Vinyl Castle', 'Современные виниловые полы с увеличенной толщиной защитного слоя и отличной устойчивостью к нагрузкам.', 'https://refloor-opt.ru/images/products/luxury-vinyl-castle.jpg');

-- Ламинат
INSERT INTO catalog_items (category, name, description, photo_url) VALUES
                                                                       ('Ламинат', 'Laminate Mountain Lake Oak', 'Классический ламинированный пол с древесным рисунком и устойчивостью к царапинам.', 'https://refloor-opt.ru/images/products/laminate-mountain-lake-oak.jpg'),
                                                                       ('Ламинат', 'Laminate Sun Dried Oak', 'Ламинат средней текстуры с улучшенной прочностью поверхности и приятным дизайном.', 'https://refloor-opt.ru/images/products/laminate-sun-dried-oak.jpg'),
                                                                       ('Ламинат', 'Laminate Twilight Oak', 'Ламинат с улучшенной влагостойкостью и реалистичным эффектом дерева.', 'https://refloor-opt.ru/images/products/laminate-twilight-oak.jpg');

-- Керамическая плитка (каменный пол)
INSERT INTO catalog_items (category, name, description, photo_url) VALUES
                                                                       ('Керамическая плитка', 'Керамогранит Stone Floor Серый', 'Керамогранит для пола с высокой прочностью и устойчивостью к нагрузкам, подходит для дома и офиса.', 'https://example.com/images/products/keramogranit-stone-grey.jpg'),
                                                                       ('Керамическая плитка', 'Керамогранит Stone Floor Бежевый', 'Плитка для пола с классическим нейтральным оттенком, универсальный выбор для любого интерьера.', 'https://example.com/images/products/keramogranit-stone-beige.jpg'),
                                                                       ('Керамическая плитка', 'Керамогранит Stone Floor Темный', 'Темный керамогранит с высокими эксплуатационными характеристиками, отличен для коридоров и входных зон.', 'https://example.com/images/products/keramogranit-stone-dark.jpg');