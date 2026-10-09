# Especificacion: Semaforos

## What (Que debe hacer el sistema)

El sistema debe implementar semaforos ubicados en intersecciones del mapa que ciclen automaticamente entre rojo, verde y amarillo con tiempos predefinidos, proporcionando senalizacion visual en la ciudad.

## Requirements (Requerimientos)

### REQ-SEM-001: Ubicacion en Intersecciones
**Prioridad**: Alta  
**Estado**: Pendiente

El sistema debe colocar semaforos en intersecciones principales del mapa 11x11:
- Posicionados en esquinas de celdas tipo calle (valor 0 en la matriz MAPA)
- Orientados hacia la direccion del trafico de cada carril
- Altura del poste: 3-4 unidades desde el suelo
- Distribuidos en 6-10 intersecciones clave

### REQ-SEM-002: Secuencia Rojo/Verde/Amarillo
**Prioridad**: Alta  
**Estado**: Pendiente

Cada semaforo debe seguir la secuencia ciclica estandar:
- **Verde**: 5 segundos — indica paso libre
- **Amarillo**: 2 segundos — indica precaucion/transicion
- **Rojo**: 5 segundos — indica detenerse
- La secuencia se repite indefinidamente: verde -> amarillo -> rojo -> verde

### REQ-SEM-003: Control por Tiempo
**Prioridad**: Alta  
**Estado**: Pendiente

El ciclo de cada semaforo debe estar controlado por tiempo real:
- Se usa el tiempo transcurrido (delta time) para avanzar el estado
- Un acumulador de tiempo por semaforo determina cuando cambiar de fase
- Los semaforos de una misma interseccion pueden estar sincronizados o desfasados segun el eje

### REQ-SEM-004: Representacion Visual
**Prioridad**: Alta  
**Estado**: Pendiente

Cada semaforo debe tener la siguiente representacion 3D:
- Poste vertical delgado de color gris oscuro (0.15f, 0.15f, 0.15f)
- Caja rectangular en la parte superior con tres circulos/luces apilados verticalmente
- Solo la luz activa tiene brillo completo; las inactivas tienen brillo reducido (20%)
- Colores: rojo (1.0f, 0.1f, 0.1f), amarillo (1.0f, 0.9f, 0.1f), verde (0.1f, 1.0f, 0.2f)

### REQ-SEM-005: Emision de Luz en Modo Noche
**Prioridad**: Media  
**Estado**: Pendiente

En modo noche, la luz activa del semaforo debe emitir un resplandor sutil:
- Radio de emision: 3-5 unidades alrededor del semaforo
- Color del resplandor coincide con la fase activa
- Intensidad reducida respecto a farolas (50% del brillo de una farola)

## Scenarios (Escenarios de Uso)

### SCENARIO-SEM-001: Ciclo Completo de un Semaforo
**Activacion**: Continuamente durante la ejecucion

```
Dado que un semaforo esta en fase verde con acumulador = 0
Cuando transcurren 5 segundos de tiempo real
Entonces el semaforo cambia a fase amarillo y el acumulador se reinicia

Dado que el semaforo esta en fase amarillo con acumulador = 0
Cuando transcurren 2 segundos de tiempo real
Entonces el semaforo cambia a fase rojo y el acumulador se reinicia

Dado que el semaforo esta en fase rojo con acumulador = 0
Cuando transcurren 5 segundos de tiempo real
Entonces el semaforo cambia a fase verde y el acumulador se reinicia
```

### SCENARIO-SEM-002: Renderizado Visual de Fase Activa
**Activacion**: Al dibujar un semaforo en cualquier frame

```
Dado que un semaforo esta en fase rojo
Cuando se ejecuta el metodo de dibujo del semaforo
Entonces la luz roja se dibuja con brillo completo (1.0f, 0.1f, 0.1f)
Y las luces amarilla y verde se dibujan con brillo reducido al 20%
```

### SCENARIO-SEM-003: Semaforos Sincronizados en Interseccion
**Activacion**: En intersecciones con dos semaforos perpendiculares

```
Dado que una interseccion tiene un semaforo en eje X y otro en eje Z
Cuando el semaforo del eje X esta en verde
Entonces el semaforo del eje Z esta en rojo
Y viceversa, con las transiciones amarillas correspondientes
```

### SCENARIO-SEM-004: Semaforos en Modo Noche
**Activacion**: Al renderizar semaforos con modoNoche = true

```
Dado que el sistema esta en modoNoche = true
Y un semaforo esta en fase verde
Cuando se renderiza la escena
Entonces el semaforo emite un resplandor verde sutil en un radio de 3-5 unidades
Y el resplandor tiene intensidad al 50% respecto a una farola
```

### SCENARIO-SEM-005: Inicio del Sistema con Semaforos
**Activacion**: Al inicializar la aplicacion

```
Dado que el mapa 11x11 ha sido cargado
Cuando se inicializan los semaforos
Entonces cada semaforo recibe una fase inicial (puede variar para evitar sincronizacion total)
Y cada semaforo tiene un offset de tiempo aleatorio entre 0 y 12 segundos
```

## Acceptance Criteria (Criterios de Aceptacion)

- [ ] Los semaforos deben estar posicionados en intersecciones del mapa 11x11
- [ ] La secuencia verde(5s) -> amarillo(2s) -> rojo(5s) debe ser correcta y ciclica
- [ ] Solo la luz activa debe tener brillo completo; las demas al 20%
- [ ] Los semaforos perpendiculares en una interseccion deben estar coordinados
- [ ] En modo noche, la luz activa debe emitir un resplandor sutil
- [ ] Los semaforos no deben afectar el rendimiento significativamente

## Context (Contexto)

Los semaforos son elementos decorativos y de senalizacion visual. No afectan la fisica del vehiculo (el jugador puede ignorarlos). Se renderizan como geometria simple (cubos y cilindros) con colores emisivos en el fragment shader.
