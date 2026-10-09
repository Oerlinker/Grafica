# Especificación: Farolas (Street Lamps)

## What (Qué debe hacer el sistema)

El sistema debe implementar farolas de calle que emitan luz circular en el suelo solo durante modo noche, añadiendo iluminación ambiental urbana con brillo decreciente hacia los bordes.

## Requirements (Requerimientos)

### REQ-FAROLAS-001: Posicionamiento de Farolas
**Prioridad**: Alta  
**Estado**: Pendiente

El sistema debe colocar farolas en intersecciones específicas del mapa 11x11:
- En esquinas de bloques principales (cada 40 unidades aproximadamente)
- Máximo 15-20 farolas distribuidas estratégicamente
- Altura estándar de farola: 4-6 unidades desde el suelo

### REQ-FAROLAS-002: Iluminación Solo en Modo Noche
**Prioridad**: Alta  
**Estado**: Pendiente

Las farolas deben emitir luz SOLO cuando modoNoche = true:
- En modo día: farolas apagadas (no emiten luz)
- En modo noche: todas las farolas activas y emitiendo luz

### REQ-FAROLAS-003: Brillo Circular en el Suelo
**Prioridad**: Alta  
**Estado**: Pendiente

Cada farola debe proyectar un patrón de luz circular en el suelo con:
- Radio aproximado: 8-10 unidades desde la base
- Brillo decreciente radialmente (brightness = 1 - distancia/radio)
- Color ámbar cálido típico de farolas (1.0f, 0.8f, 0.3f para luz, 1.0f, 0.5f, 0.2f para proyección)

### REQ-FAROLAS-004: Forma y Material
**Prioridad**: Media  
**Estado**: Pendiente

Cada farola debe tener:
- Poste cilíndrico simple (aproximadamente 10x1 unidades de base, 6 unidades alto)
- Color gris metálico oscuro (0.2f, 0.2f, 0.2f)
- Luminarias en la parte superior con forma de U o caja simple

### REQ-FAROLAS-005: Uniformes de OpenGL
**Prioridad**: Alta  
**Estado**: Pendiente

El sistema debe gestionar los siguientes uniformes para farolas:
- `uFarolas[]`: Array de posiciones 3D (x, y, z) de cada farola activa
- `uFarolasCount`: Número de farolas activas en escena

## Scenarios (Escenarios de Uso)

### SCENARIO-FAROLAS-001: Activación en Modo Noche
**Activación**: Al cambiar a modo noche o al renderizar con modoNoche = true

```
Dado que el sistema tiene farolas posicionadas en intersecciones del mapa 11x11
Y el estado es modoNoche = true
Cuando se ejecuta dibujarFrame()
Entonces cada farola activa proyecta luz circular en el suelo desde su posición (x, y, z)
Con radio de aproximadamente 8 unidades y color ámbar (1.0f, 0.8f, 0.3f)
```

### SCENARIO-FAROLAS-002: Farolas Apagadas en Modo Día
**Activación**: Al cambiar a modo día o al renderizar con modoNoche = false

```
Dado que el sistema tiene farolas posicionadas en intersecciones del mapa 11x11
Y el estado es modoNoche = false (modo día)
Cuando se ejecuta dibujarFrame()
Entonces las farolas no emiten luz visible
O bien: las farolas siguen dibujadas pero sin efecto de iluminación activa
```

### SCENARIO-FAROLAS-003: Brillo Decreciente
**Activación**: Al renderizar cerca o lejos de una farola

```
Dado que hay una farola en posición (x_f, y_f, z_f)
Y se está dibujando un píxel en la posición P = (x_p, y_p, z_p)
Cuando la distancia d entre P y la farola es menor al radio R
Entonces el brillo del píxel es: brightness = max(0, 1 - d/R)
Y el color final es: uFarolaColor * brightness
```

### SCENARIO-FAROLAS-004: Distribución Estratégica en Mapa Ampliado
**Activación**: Al cargar el mapa 11x11 ampliado

```
Dado que el mapa tiene dimensiones 11x11 (220x220 unidades)
Cuando se inicializan las farolas
Entonces se colocan aproximadamente 15-20 farolas en intersecciones principales
Distribuidas cada ~40 unidades para cubrir el mapa ampliado
```

## Acceptance Criteria (Criterios de Aceptación)

- [ ] Las farolas solo emiten luz en modo noche (modo día: apagadas o sin efecto visual)
- [ ] La proyección de luz debe ser circular con brillo decreciente hacia los bordes
- [ ] Deben haber entre 15-20 farolas distribuidas estratégicamente en el mapa 11x11
- [ ] El color debe ser ámbar cálido típico de iluminación de calle (1.0, 0.8, 0.3)
- [ ] Las farolas deben posicionarse en intersecciones naturales del mapa urbano

## Context (Contexto)

Las farolas se integran con el shader existente añadiendo:
- Un array de posiciones en GPU para acceso eficiente por vertex
- Lógica en el fragment shader para calcular distancias y atenuación radial
- Dependencia del estado modoNoche para activar/desactivar la iluminación
