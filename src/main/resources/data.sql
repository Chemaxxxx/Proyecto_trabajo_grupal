
INSERT IGNORE INTO pasillos (id, numero, almacen_id) VALUES
(1, 1, 1),
(2, 2, 1),
(3, 3, 2);


INSERT IGNORE INTO estanterias (id, material, lleno, pasillo_id) VALUES
(1, 'Acero galvanizado', false, 1),
(2, 'Acero galvanizado', true, 1),
(3, 'Madera reforzada', false, 2),
(4, 'Aluminio', true, 3);