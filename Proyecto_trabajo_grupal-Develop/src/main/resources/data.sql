
INSERT IGNORE INTO pasillos (id, numero, almacen_id) VALUES
(1, 1, 1),
(2, 2, 1),
(3, 3, 2);


INSERT IGNORE INTO estanterias (id, material, lleno, pasillo_id) VALUES
(1, 'Acero galvanizado', false, 1),
(2, 'Acero galvanizado', true, 1),
(3, 'Madera reforzada', false, 2),
(4, 'Aluminio', true, 3);


INSERT IGNORE INTO cajas (id, peso, contenido, estanteria_id) VALUES
(1, 12.5, 'Tornillos M6', 1),
(2, 3.2, 'Guantes de trabajo', 1),
(3, 25.0, 'Bobinas de cable', 3);

INSERT IGNORE INTO etiquetas_producto (id, codigo_barras, caja_id) VALUES
(1, '8412345678905', 1);
