-- Удаляем старые записи
DELETE FROM catalog_items;

-- Наполняем каталог полами

-- Кварцевый ламинат SPC
INSERT INTO catalog_items (category, name, description, photo_url) VALUES
                                                                       ('QUARTZ_LAMINATE', 'SPC Fargo Herringbone Дуб Рокфорд', 'Каменный SPC ламинат с водостойкостью и высокой износостойкостью, класс 42. Подходит для квартиры и коммерческих помещений.', 'https://refloor-opt.ru/images/products/spc-fargo-herringbone-rokford.jpg'),
                                                                       ('QUARTZ_LAMINATE', 'SPC Fargo Herringbone Дуб Верде', 'Кварцевый SPC ламинат с эстетичным рисунком дерева и повышенной прочностью.', 'https://refloor-opt.ru/images/products/spc-fargo-herringbone-verde.jpg'),
                                                                       ('QUARTZ_LAMINATE', 'SPC Fargo Herringbone Дуб Борнео', 'SPC ламинат каменно‑полимерного типа, стойкий к влаге и износу, легко укладывается.', 'https://refloor-opt.ru/images/products/spc-fargo-herringbone-borneo.jpg');

-- Керамическая плитка (каменный пол)
INSERT INTO catalog_items (category, name, description, photo_url) VALUES
                                                                       ('TILE', 'Керамогранит Stone Floor Серый', 'Керамогранит для пола с высокой прочностью и устойчивостью к нагрузкам, подходит для дома и офиса.', 'https://example.com/images/products/keramogranit-stone-grey.jpg'),
                                                                       ('TILE', 'Керамогранит Stone Floor Бежевый', 'Плитка для пола с классическим нейтральным оттенком, универсальный выбор для любого интерьера.', 'https://example.com/images/products/keramogranit-stone-beige.jpg'),
                                                                       ('TILE', 'Керамогранит Stone Floor Темный', 'Темный керамогранит с высокими эксплуатационными характеристиками, отличен для коридоров и входных зон.', 'https://example.com/images/products/keramogranit-stone-dark.jpg');