# Propuesta: Ampliar Ciudad Interactiva

## Why (Motivación)

La ciudad interactiva actual utiliza un mapa de 7x7 celdas con edificios simples y visualización estática. Para crear una experiencia urbana más inmersiva y realista, necesitamos expandir el mapa a 11x11 celdas e introducir nuevos sistemas visuales dinámicos como iluminación día/noche, decoración urbana funcional (farolas), elementos atmosféricos (faro), semáforos animados y un minimapa para navegación. Esta ampliación mejora significativamente la inmersión del jugador al transformar una representación abstracta de ciudad en un entorno urbano vivo y dinámico.

## What Changes (Cambios)

### Nuevas Capacidades
- **Mapa ampliado**: Expansión de la cuadrícula de 7x7 a 11x11 celdas con nuevos edificios, parques y decoración urbana
- **Iluminación dinámica día/noche**: Sistema que alterna entre dos modos visuales con transiciones suaves
- **Farolas (street lamps)**: Iluminación ambiental solo activa en modo noche
- **Iluminación del vehículo**: Faros delanteros para mejor visibilidad en oscuridad
- **Semáforos animados**: Semaforo funcional que cambia entre rojo/verde con estados de transición
- **Faro (lighthouse)**: Torre faro con luz rotatoria y haz giratorio como elemento decorativo costero
- **Minimapa overhead**: Vista superior para navegación contextual

### Modificaciones a Capacidad Existente
N/A - Esta ampliación introduce nuevas capacidades sin modificar requerimientos existentes.

## Capabilities (Capacidades)

### New Capabilities

#### mapa-ampliado
Expansión de la cuadrícula del mapa de 7x7 a 11x11 celdas, añadiendo nuevos edificios, parques, y decoración urbana variada para mayor diversidad visual y espacial.

#### iluminacion-dia-noche
Sistema de alternancia entre modos día y noche con transiciones suaves, cambiando la paleta de colores ambiental, visibilidad general y comportamiento de otros sistemas de iluminación.

#### farolas
Lámparas de calle que emiten luz circular en el suelo solo durante modo noche, añadiendo iluminación ambiental urbana con brillo decreciente hacia los bordes.

#### iluminacion-vehiculo
Faros delanteros del vehículo que proyectan cono luminoso frontal para mejorar visibilidad en condiciones de poca luz o oscuridad total.

#### semaforos
Semáforo animado funcional que alterna entre estados rojo/verde con transiciones suaves, añadiendo interactividad urbana dinámica.

#### faro
Torre faro decorativa con luz rotatoria y haz giratorio que emite rayo cónico iluminando áreas costeras o de borde del mapa.

#### minimapa
Vista overhead superpuesta que muestra la cuadrícula completa del mapa para navegación contextual, indicando posición relativa del jugador y límites del mundo.

## Impact (Impacto)

### Código Afectado
- `AppCiudad.java`: Expansión de matriz MAPA de 7x7 a 11x11, implementación de sistemas de iluminación dinámica, farolas, semáforos, faro y minimapa
- Shaders: Modificaciones para soportar iluminación dinámica (colores ambientales día/noche), emisores de luz adicionales y transformación de coordenadas para minimapa

### APIs Afectadas
- `GLFW.glfw*`: Configuración adicional para control de visibilidad (modo noche puede requerir ajustar contraste)
- OpenGL: Nuevos uniformes para variables de iluminación, estados día/noche, posiciones de farolas y semáforos

### Dependencias
Ninguna dependencia externa añadida. Todo el cambio se implementa usando LWJGL + OpenGL existente.

### Sistema
Aumento en número de objetos dibujados (aproximadamente 4x más celdas = ~70% más vértices). El sistema mantiene la misma arquitectura basada en matrices, solo escala los tamaños y añade nuevos sistemas.
