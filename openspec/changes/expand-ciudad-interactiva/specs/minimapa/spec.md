# Especificacion: Minimapa

## What (Que debe hacer el sistema)

El sistema debe renderizar una vista superior (top-down) en una esquina de la pantalla que muestre toda la ciudad, la posicion y orientacion del vehiculo, el destino activo si existe, con el norte siempre arriba. El minimapa se puede mostrar/ocultar con la tecla M y se adapta al redimensionado de ventana.

## Requirements (Requerimientos)

### REQ-MINI-001: Vista Superior Completa
**Prioridad**: Alta  
**Estado**: Pendiente

El minimapa debe mostrar una representacion esquematica de toda la ciudad desde arriba:
- Proyeccion ortografica top-down (eje Y hacia abajo en pantalla)
- Muestra la cuadricula completa 11x11 del mapa
- Edificios como rectangulos grises, parques como rectangulos verdes, calles como fondo oscuro
- Escala uniforme que encaje toda la ciudad en el area del minimapa

### REQ-MINI-002: Posicion en Esquina de Pantalla
**Prioridad**: Alta  
**Estado**: Pendiente

El minimapa debe posicionarse en una esquina de la ventana:
- Ubicacion: esquina inferior derecha (o superior derecha)
- Tamano: 150x150 a 200x200 pixeles
- Margen de 10-15 pixeles desde los bordes de la ventana
- Fondo semitransparente con borde visible para distinguirlo de la escena principal

### REQ-MINI-003: Indicador de Posicion y Orientacion del Vehiculo
**Prioridad**: Alta  
**Estado**: Pendiente

El minimapa debe mostrar el vehiculo como un indicador claro:
- Icono: triangulo o flecha que indique la direccion del vehiculo
- Color: amarillo o blanco brillante para destacar sobre el mapa
- Posicion actualizada en tiempo real segun las coordenadas del vehiculo
- Rotacion del icono segun el angulo actual del vehiculo

### REQ-MINI-004: Indicador de Destino Activo
**Prioridad**: Media  
**Estado**: Pendiente

Si existe un destino activo en el sistema de navegacion:
- Mostrar el destino como un punto o marcador de color rojo
- El marcador parpadea o tiene un halo para ser facilmente identificable
- Si no hay destino activo, no se muestra ningun marcador

### REQ-MINI-005: Norte Siempre Arriba
**Prioridad**: Alta  
**Estado**: Pendiente

El minimapa debe mantener una orientacion fija:
- El norte (eje Z negativo o positivo segun convencion) siempre apunta hacia arriba
- El mapa no rota con el vehiculo; solo el icono del vehiculo rota
- Esto permite al jugador orientarse rapidamente

### REQ-MINI-006: Toggle con Tecla M
**Prioridad**: Alta  
**Estado**: Pendiente

El minimapa debe poder mostrarse y ocultarse:
- Tecla M alterna la visibilidad del minimapa
- Estado inicial: visible (minimapaVisible = true)
- El toggle no afecta ningun otro sistema

### REQ-MINI-007: Adaptacion al Resize de Ventana
**Prioridad**: Media  
**Estado**: Pendiente

El minimapa debe adaptarse al redimensionado de la ventana:
- Mantiene su posicion relativa en la esquina
- Mantiene su tamano en pixeles (no escala proporcionalmente con la ventana)
- Recalcula el viewport del minimapa al recibir un evento de resize

## Scenarios (Escenarios de Uso)

### SCENARIO-MINI-001: Renderizado del Minimapa
**Activacion**: En cada frame mientras minimapaVisible = true

```
Dado que minimapaVisible = true
Y la ventana tiene dimensiones W x H
Cuando se ejecuta dibujarFrame()
Entonces se configura glViewport para un area de 180x180 px en la esquina inferior derecha
Y se aplica una proyeccion ortografica que abarca toda la cuadricula 11x11
Y se dibujan los bloques del mapa como rectangulos coloreados segun su tipo
Y se dibuja el icono del vehiculo en la posicion y orientacion correctas
```

### SCENARIO-MINI-002: Mostrar/Ocultar Minimapa
**Activacion**: Al pulsar la tecla M

```
Dado que minimapaVisible = true
Cuando el jugador pulsa la tecla M
Entonces minimapaVisible cambia a false
Y el minimapa deja de renderizarse

Dado que minimapaVisible = false
Cuando el jugador pulsa la tecla M
Entonces minimapaVisible cambia a true
Y el minimapa se renderiza nuevamente en la esquina
```

### SCENARIO-MINI-003: Vehiculo en Movimiento
**Activacion**: Mientras el vehiculo se desplaza con minimapa visible

```
Dado que el minimapa es visible
Y el vehiculo esta en posicion (vx, vz) con angulo A
Cuando el vehiculo se mueve a nueva posicion (vx', vz') con angulo A'
Entonces el icono del vehiculo en el minimapa se actualiza a la nueva posicion mapeada
Y la rotacion del icono refleja el nuevo angulo A'
```

### SCENARIO-MINI-004: Destino Activo Visible
**Activacion**: Cuando el sistema de navegacion tiene un destino asignado

```
Dado que el minimapa es visible
Y el sistema de navegacion tiene un destino en posicion (dx, dz)
Cuando se renderiza el minimapa
Entonces se dibuja un marcador rojo en la posicion (dx, dz) mapeada al minimapa
Y el marcador tiene un efecto visual de pulsacion o halo
```

### SCENARIO-MINI-005: Redimensionado de Ventana
**Activacion**: Al cambiar el tamano de la ventana

```
Dado que el minimapa es visible en la esquina inferior derecha
Cuando la ventana cambia de tamano de (W, H) a (W', H')
Entonces el minimapa recalcula su viewport para mantenerse en la esquina inferior derecha
Y el tamano del minimapa permanece en 180x180 pixeles
Y el margen desde los bordes se mantiene constante
```

## Acceptance Criteria (Criterios de Aceptacion)

- [ ] El minimapa debe mostrar toda la cuadricula 11x11 en vista top-down
- [ ] La posicion del vehiculo debe actualizarse en tiempo real con orientacion visible
- [ ] El norte debe estar siempre arriba independientemente de la orientacion del vehiculo
- [ ] La tecla M debe alternar la visibilidad sin afectar otros sistemas
- [ ] El minimapa debe mantener su posicion en la esquina al redimensionar la ventana
- [ ] El destino activo debe ser visible como marcador distinguible cuando exista
- [ ] El minimapa no debe degradar significativamente el rendimiento (renderizado ligero)

## Context (Contexto)

El minimapa se implementa usando un segundo pase de renderizado con su propio viewport y proyeccion ortografica. Se dibuja despues de la escena principal, sobre ella, usando glViewport para delimitar el area. No requiere un framebuffer separado; basta con cambiar viewport y proyeccion temporalmente.
