# Especificacion: Faros del Vehiculo

## What (Que debe hacer el sistema)

El sistema debe implementar dos focos delanteros en el vehiculo que sigan su posicion y orientacion, proyectando luz hacia adelante con alcance limitado. El jugador puede encender/apagar los faros con la tecla F.

## Requirements (Requerimientos)

### REQ-FAROS-001: Dos Focos Delanteros
**Prioridad**: Alta  
**Estado**: Pendiente

El sistema debe renderizar dos focos de luz en la parte delantera del vehiculo:
- Separados simetricamente respecto al eje longitudinal del auto (aprox. 0.3-0.5 unidades a cada lado)
- Posicion vertical ligeramente por encima del suelo (aprox. 0.5-0.8 unidades)
- Cada foco emite un cono de luz independiente

### REQ-FAROS-002: Seguimiento de Posicion y Orientacion
**Prioridad**: Alta  
**Estado**: Pendiente

Los focos deben seguir la posicion y orientacion del vehiculo en todo momento:
- La posicion de cada foco se calcula a partir de la posicion del auto mas un offset rotado segun el angulo actual
- La direccion de proyeccion coincide con el vector "adelante" del vehiculo
- Al girar el auto, los faros giran solidariamente

### REQ-FAROS-003: Toggle con Tecla F
**Prioridad**: Alta  
**Estado**: Pendiente

El sistema debe permitir encender y apagar los faros con la tecla F:
- Estado inicial: apagados
- Al pulsar F se alterna entre encendido y apagado
- El estado se guarda en un booleano `farosEncendidos`

### REQ-FAROS-004: Alcance Limitado y Proyeccion
**Prioridad**: Alta  
**Estado**: Pendiente

Cada faro debe proyectar luz con las siguientes caracteristicas:
- Alcance maximo: 15-20 unidades hacia adelante
- Angulo del cono de luz: 25-35 grados desde el eje central
- Atenuacion gradual con la distancia (brightness = max(0, 1 - d/alcance))
- Color blanco calido (1.0f, 0.95f, 0.8f)

### REQ-FAROS-005: Uniformes de OpenGL
**Prioridad**: Alta  
**Estado**: Pendiente

El sistema debe gestionar los siguientes uniformes para los faros:
- `uFarosPos[2]`: Posiciones 3D de cada foco
- `uFarosDir[2]`: Direcciones de proyeccion de cada foco
- `uFarosEncendidos`: Booleano indicando si los faros estan activos
- `uFarosAlcance`: Alcance maximo de la luz
- `uFarosAngulo`: Angulo del cono en radianes

## Scenarios (Escenarios de Uso)

### SCENARIO-FAROS-001: Encender Faros
**Activacion**: Al pulsar la tecla F con faros apagados

```
Dado que el vehiculo esta en la escena con farosEncendidos = false
Cuando el jugador pulsa la tecla F
Entonces farosEncendidos cambia a true
Y los dos focos delanteros comienzan a proyectar luz hacia adelante
```

### SCENARIO-FAROS-002: Apagar Faros
**Activacion**: Al pulsar la tecla F con faros encendidos

```
Dado que el vehiculo esta en la escena con farosEncendidos = true
Cuando el jugador pulsa la tecla F
Entonces farosEncendidos cambia a false
Y los dos focos delanteros dejan de emitir luz
```

### SCENARIO-FAROS-003: Faros Siguen la Orientacion del Vehiculo
**Activacion**: Al girar el vehiculo con faros encendidos

```
Dado que el vehiculo tiene farosEncendidos = true
Y el vehiculo esta orientado a un angulo A
Cuando el vehiculo gira a un nuevo angulo A'
Entonces las posiciones de los focos se recalculan con el nuevo angulo
Y la direccion de proyeccion de ambos focos apunta en la nueva direccion frontal
```

### SCENARIO-FAROS-004: Atenuacion por Distancia
**Activacion**: Al renderizar superficies dentro del alcance de los faros

```
Dado que los faros estan encendidos y proyectan luz
Y hay una superficie a distancia d del foco
Cuando d < alcance maximo (20 unidades)
Entonces el brillo en esa superficie es max(0, 1 - d/alcance)
Y el brillo se multiplica por el factor angular del cono

Dado que los faros estan encendidos
Y hay una superficie a distancia d >= alcance maximo
Cuando se calcula la iluminacion
Entonces la superficie no recibe luz de los faros
```

### SCENARIO-FAROS-005: Faros en Combinacion con Modo Noche
**Activacion**: Al usar faros durante modo noche

```
Dado que el sistema esta en modoNoche = true
Y el vehiculo tiene farosEncendidos = true
Cuando se renderiza la escena
Entonces los faros del vehiculo contribuyen a la iluminacion junto con las farolas
Y los faros son la principal fuente de luz movil en la escena
```

## Acceptance Criteria (Criterios de Aceptacion)

- [ ] Se deben renderizar exactamente dos focos delanteros en el vehiculo
- [ ] Los focos deben seguir la posicion y orientacion del auto en todo momento
- [ ] La tecla F debe alternar entre encendido y apagado sin retardo perceptible
- [ ] El alcance de la luz debe estar limitado a 15-20 unidades
- [ ] La atenuacion debe ser gradual tanto por distancia como por angulo del cono
- [ ] Los faros deben funcionar correctamente en combinacion con el sistema dia/noche

## Context (Contexto)

Los faros se integran con el shader existente anadiendo:
- Dos luces tipo spotlight en el fragment shader con calculo de cono y atenuacion
- Actualizacion de uniformes cada frame segun la posicion y angulo del vehiculo
- Logica de toggle en el callback de teclado existente
