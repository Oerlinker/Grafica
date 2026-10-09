# Especificación: Ampliación de Mapa

## What (Qué debe hacer el sistema)

El sistema debe ampliar la cuadrícula del mapa desde 7x7 a 11x11 celdas, implementando nuevos edificios, parques y decoración urbana variada para toda la superficie expandida.

## Requirements (Requerimientos)

### REQ-MAPA-001: Ampliación de Cuadrícula
**Prioridad**: Alta  
**Estado**: Pendiente

El sistema debe expandir el mapa de 7x7 a 11x11 celdas, donde cada celda mide 10 unidades (CELDA = 10) y el límite del mundo es 35 unidades desde el origen. Esto implica:
- Nuevo tamaño: 220 unidades por lado (de 70 actual)
- Matriz MAPA de 11x11 en lugar de 7x7

### REQ-MAPA-002: Generación de Contenido Urbano Variado
**Prioridad**: Alta  
**Estado**: Pendiente

El sistema debe poblar las celdas del mapa con contenido urbano diverso:
- **Edificios**: Edificios comerciales/residenciales estándar
- **Parques**: Áreas verdes en patrones distribuidos
- **Decoración urbana**: Elementos decorativos (banquetas, farolas base)

### REQ-MAPA-003: Coordenadas del Mapa Ampliado
**Prioridad**: Alta  
**Estado**: Pendiente

El sistema debe calcular las posiciones correctas para cada celda en la cuadrícula 11x11 usando la fórmula:
```
centro(columna, fila) = -LIMITE + CELDA * (columna + 0.5)
```

Donde:
- LIMITE = 35 unidades desde el origen
- CELDA = 10 unidades por celda
- columnas y filas: 0 a 10

## Scenarios (Escenarios de Uso)

### SCENARIO-MAPA-001: Renderizado Completo del Mapa Ampliado
**Activación**: Al iniciar o al cargar el mapa

```
Dado que el sistema ha inicializado OpenGL
Y la matriz MAPA tiene dimensiones 11x11
Cuando se ejecuta el método actualizar()
Entonces el sistema dibuja 121 celdas (11x11) en lugar de 49 (7x7)
```

### SCENARIO-MAPA-002: Distribución de Contenido Urbano
**Activación**: Al renderizar cada celda del mapa ampliado

```
Dado que el sistema está dibujando una celda específica
Cuando la celda corresponde a un edificio comercial
Entonces se dibuja un bloque rectangular con altura entre 3 y 8 unidades
Y el color es gris claro (0.65, 0.70, 0.75)

Dado que el sistema está dibujando una celda específica
Cuando la celda corresponde a un parque
Entonces se dibuja terreno verde (0.20, 0.40, 0.15) con árboles simples
Y bordes de senderos en color café (0.35, 0.28, 0.15)
```

### SCENARIO-MAPA-003: Transición entre Mapa Anterior y Nuevo
**Activación**: Al primer renderizado después de ampliación

```
Dado que el mapa anterior era 7x7 (49 celdas)
Y el nuevo mapa es 11x11 (121 celdas)
Cuando se inicia la aplicación ampliado
Entonces la nueva matriz MAPA[0..=][0..=] contiene valores válidos (0 a 3)
```

## Acceptance Criteria (Criterios de Aceptación)

- [ ] El mapa debe mostrar exactamente 121 celdas organizadas en 11x11
- [ ] Cada celda debe tener coordenadas correctas según la fórmula centro()
- [ ] La distribución de edificios, parques y decoración debe ser variada
- [ ] Los objetos deben estar posicionados correctamente en el espacio 3D
- [ ] No debe haber solapamiento o errores de profundidad entre objetos

## Context (Contexto)

Esta especificación extiende la implementación actual de `AppCiudad.java` donde:
- El mapa actual es 7x7 con valores tipo: 0=calle, 1=edificio alto, 2=edificio bajo, 3=decoración
- Cada celda mide CELDA = 10 unidades
- El límite del mundo es LIMITE = 35 unidades desde el origen
