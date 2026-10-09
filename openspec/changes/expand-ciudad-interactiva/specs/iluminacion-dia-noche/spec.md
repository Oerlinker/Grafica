# Especificación: Iluminación Dinámica Día/Noche

## What (Qué debe hacer el sistema)

El sistema debe implementar un ciclo día/noche dinámico que alterne entre dos modos visuales con transiciones suaves, cambiando la paleta de colores ambiental y comportamiento de otros sistemas.

## Requirements (Requerimientos)

### REQ-ILUM-DIA-NOCHE-001: Modo Día
**Prioridad**: Alta  
**Estado**: Pendiente

El sistema debe implementar un modo día con las siguientes características:
- Fondo ambiente claro (cielo azul, 0.5f, 0.7f, 0.9f)
- Iluminación general brillante
- Sombras suaves y visibles
- Colores saturados naturales

### REQ-ILUM-DIA-NOCHE-002: Modo Noche  
**Prioridad**: Alta  
**Estado**: Pendiente

El sistema debe implementar un modo noche con las siguientes características:
- Fondo ambiente oscuro (cielo nocturno, 0.05f, 0.1f, 0.2f)
- Iluminación general reducida al 20-30%
- Alta dependencia de fuentes de luz artificial
- Colores más fríos y desaturados

### REQ-ILUM-DIA-NOCHE-003: Transiciones Suaves
**Prioridad**: Media  
**Estado**: Pendiente

El sistema debe implementar transiciones suaves entre modos día/noche mediante interpolación gradual de los valores RGB del fondo ambiente sobre 3-5 segundos.

### REQ-ILUM-DIA-NOCHE-004: Control de Estado
**Prioridad**: Alta  
**Estado**: Pendiente

El sistema debe mantener un estado booleano `modoNoche` que controle qué modo visual está activo en cada momento.

## Scenarios (Escenarios de Uso)

### SCENARIO-DIA-NOCHE-001: Cambio entre Modos
**Activación**: Al pulsar tecla asignada o por tiempo automático

```
Dado que el sistema tiene estado modoNoche = false (modo día)
Y la ventana es visible para renderizar
Cuando se activa el modo noche
Entonces glClearColor cambia gradualmente de (0.5f, 0.7f, 0.9f) a (0.05f, 0.1f, 0.2f)
Y la visibilidad general se reduce al 20-30%
```

### SCENARIO-DIA-NOCHE-002: Renderizado en Modo Día
**Activación**: Al renderizar mientras modoNoche = false

```
Dado que el sistema está en modo día
Cuando se ejecuta dibujarFrame()
Entonces glClearColor(0.5f, 0.7f, 0.9f, 1) se aplica al buffer de color
Y los objetos tienen visibilidad completa (100%)
```

### SCENARIO-DIA-NOCHE-003: Renderizado en Modo Noche
**Activación**: Al renderizar mientras modoNoche = true

```
Dado que el sistema está en modo noche
Cuando se ejecuta dibujarFrame()
Entonces glClearColor(0.05f, 0.1f, 0.2f, 1) se aplica al buffer de color
Y los objetos tienen visibilidad reducida (20-30%)
```

### SCENARIO-DIA-NOCHE-004: Transición Suave entre Modos
**Activación**: Durante el periodo de transición

```
Dado que el sistema está en proceso de transición día→noche
Cuando se renderiza cada cuadro durante la transición
Entonces los colores de fondo se interpolan linealmente entre valores inicial y final
Y la duración total de la transición es aproximadamente 4 segundos
```

## Acceptance Criteria (Criterios de Aceptación)

- [ ] El modo día debe mostrar cielo azul claro con visibilidad completa
- [ ] El modo noche debe mostrar cielo oscuro con dependencia de fuentes de luz artificial
- [ ] Las transiciones entre modos deben ser suaves y continuas
- [ ] El estado actual del modo debe ser controlable por tecla o automáticamente
- [ ] No deben haber parpadeos bruscos ni artefactos visuales durante transiciones

## Context (Contexto)

Esta especificación introduce un sistema de iluminación temporal que afecta a toda la escena. Se integrará con el shader existente para recibir nuevos uniformes de iluminación ambiente y estado del modo día/noche.
