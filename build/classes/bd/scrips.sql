-- Crear base de datos
CREATE DATABASE arq_nanfai;
USE arq_nanfai;

-- Tabla: Rol
CREATE TABLE rol (
    idRol INT PRIMARY KEY AUTO_INCREMENT,
    nombreRol VARCHAR(50) NOT NULL
);

-- Insertar roles
INSERT INTO rol (nombreRol) VALUES 
('dueño'),
('asistente'),
('empleado');

-- Tabla: Usuario
CREATE TABLE usuario (
    idUsuario INT PRIMARY KEY AUTO_INCREMENT,
    nombreUsuario VARCHAR(100) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    contrasena VARCHAR(200) NOT NULL,
    idRol INT NOT NULL,
    FOREIGN KEY (idRol) REFERENCES rol(idRol)
);

-- Tabla: Categoria
CREATE TABLE categoria (
    idCategoria INT PRIMARY KEY AUTO_INCREMENT,
    nombreCategoria VARCHAR(100) NOT NULL
);

-- Insertar categorías de prueba
INSERT INTO categoria (nombreCategoria) VALUES 
('bebidas'), 
('alimentos'), 
('limpieza');

-- Tabla: Proveedor
CREATE TABLE proveedor (
    idProveedores INT PRIMARY KEY AUTO_INCREMENT,
    nombreProveedor VARCHAR(100) NOT NULL,
    telefono VARCHAR(15),
    direccion TEXT
);

-- Insertar proveedor de prueba
INSERT INTO proveedor (nombreProveedor, telefono, direccion) VALUES 
('Distribuidora ABC', '999999999', 'Av. Principal 123');

-- Tabla: Ubicacion (opcional para esta fase, se puede omitir si no lo usarás aún)
CREATE TABLE ubicacion (
    idUbicacion INT PRIMARY KEY AUTO_INCREMENT,
    tipoProducto VARCHAR(50),
    codigoAndamio VARCHAR(10)
);

-- Tabla: Producto
CREATE TABLE producto (
    idProducto INT PRIMARY KEY AUTO_INCREMENT,
    nombreProducto VARCHAR(100) NOT NULL,
    idCategoria INT NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    cantidad INT NOT NULL,
    fechaIngreso DATE NOT NULL,
    fechaVencimiento DATE,
    idProveedor INT NOT NULL,
    idUbicacion INT,
    estado BIT NOT NULL DEFAULT 1,
    FOREIGN KEY (idCategoria) REFERENCES categoria(idCategoria),
    FOREIGN KEY (idProveedor) REFERENCES proveedor(idProveedores),
    FOREIGN KEY (idUbicacion) REFERENCES ubicacion(idUbicacion)
);

-- Usuario de prueba para login

INSERT INTO usuario (nombreUsuario, correo, contrasena, idRol)
VALUES ('Administrador', 'admin@nanf.com', '123456', 1); -- Rol 1 = dueño



