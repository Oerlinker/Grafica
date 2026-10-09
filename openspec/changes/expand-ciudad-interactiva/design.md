# Design: Ampliar Ciudad Interactiva

## Context

El proyecto actual consiste en una ciudad 3D interactiva construida con Java + LWJGL + OpenGL 3.3 Core Profile. La clase principal `AppCiudad.java` renderiza una cuadricula de 7x7 celdas usando una matriz `MAPA[][]` donde cada celda mide 10 unidades (`CELDA = 10`) y el limite del mundo es 35 unidades desde el origen (`LIMITE = 35`). Los tipos de celda son: 0 = calle, 1 = edificio alto, 2 = edificio bajo/parque.

El renderizado se hace con un unico programa de shaders. El vertex shader soporta dos modos: perspectiva normal (`uMapa = 0`) y una proyeccion ortografica basica para minimapa (`uMapa = 1`). El fragment shader actual es minimal: solo aplica `uColor` sin iluminacion. La camara orbita alrededor del centro con flechas izquierda/derecha.

El sistema usa un VBO con un cubo unitario de 36 vertices (posicion + normal) y dibuja cada objeto con uniforms (`uPos`, `uEscala`, `uColor`, `uGiro`) seguidos de `glDrawArrays`. No hay vehiculo controlable, no hay iluminacion dinamica, no hay ciclo dia/noche.

Esta ampliacion necesita:
- Expandir el mapa a 11x11 con nuevos tipos de celda y contenido variado
- Agregar un vehiculo controlable con camara en tercera persona
- Implementar ciclo dia/noche con transiciones suaves
- Agregar multiples fuentes de luz (farolas, faros del vehiculo, semaforos)
- Renderizar decoracion urbana (arboles, bancos, ventanas, pasos peatonales)
- Implementar semaforos animados con secuencia temporal
- Agregar un minimapa funcional con viewport separado

## Goals

1. **Mapa ampliado 11x11**: Expandir la cuadricula con edificios, parques y decoracion diversa, recalculando `LIMITE` y la formula `centro()` para el nuevo tamano.

2. **Iluminacion dia/noche**: Ciclo visual con transicion suave entre modo dia (cielo azul, visibilidad completa) y modo noche (cielo oscuro, dependencia de luces artificiales).

3. **Farolas**: 15-20 lamparas de calle en intersecciones que emiten luz circular ambar solo en modo noche, con atenuacion radial.

4. **Faros del vehiculo**: Dos focos delanteros tipo spotlight que siguen posicion y orientacion del auto, toggle con F, alcance de 15-20 unidades.

5. **Decoracion urbana**: Arboles y bancos en parques, ventanas en edificios (iluminadas de noche), pasos peatonales en calles junto a manzanas.

6. **Semaforos**: Semaforos en intersecciones con secuencia rojo/verde/amarillo controlada por tiempo, coordinacion perpendicular.

7. **Minimapa**: Vista top-down en esquina de pantalla, toggle con M, norte arriba, posicion del vehiculo, adaptacion a resize.

8. **Reinicio**: Tecla para reiniciar la posicion del vehiculo y estado del mundo.

## Non-Goals

- **No se cambia el stack tecnologico**: Se mantiene Java + LWJGL + OpenGL 3.3. No se migra a Vulkan, LibGDX ni otro framework.
- **No se agrega multiplayer**: El vehiculo es controlado por un solo jugador local.
- **No se implementa fisica avanzada**: No hay colisiones con edificios, gravedad realista ni dinamica de vehiculos. El auto se mueve libremente.
- **No se implementa audio**: No hay efectos de sonido ni musica ambiental.
- **No se usan texturas**: Todo se renderiza con colores solidos via uniforms, como en el sistema actual.
- **No se implementa sombras reales**: La iluminacion es aditiva simplificada, no shadow mapping.
- **Los semaforos no afectan al vehiculo**: Son puramente decorativos/visuales.

## Decisions

### D1: Clase unica vs. multiples clases

**Decision**: Mantener la logica principal en `AppCiudad.java` pero extraer el vehiculo a una clase `Vehiculo.java` separada.

**Razon**: El vehiculo tiene estado propio (posicion, angulo, velocidad, faros) y logica de actualizacion independiente. Extraerlo reduce la complejidad de la clase principal sin fragmentar excesivamente el codigo. El resto de sistemas (farolas, semaforos, decoracion) se implementan como metodos dentro de `AppCiudad` porque son parte del renderizado de la escena y dependen fuertemente del estado global (modoNoche, MAPA, uniforms).

### D2: Grid 11x11 — constantes y formula

**Decision**: Cambiar `LIMITE` de 35 a 55 y mantener `CELDA = 10`. La formula `centro(indice)` sigue siendo `-LIMITE + CELDA * (indice + 0.5f)` pero ahora con indices de 0 a 10.

**Razon**: Mantener la misma formula simplifica la transicion. El suelo y la distancia de camara se ajustan proporcionalmente. El mapa pasa de 70x70 a 110x110 unidades.

### D3: Iluminacion en el fragment shader

**Decision**: Reescribir el fragment shader para calcular iluminacion por fragmento con multiples fuentes de luz. Se usa un modelo simplificado: luz ambiente global + contribuciones puntuales/conicas de farolas y faros.

**Implementacion**:
- Uniform `uModoNoche` (float, 0.0 a 1.0) para interpolar entre dia y noche
- Uniform `uLuzAmbiente` (vec3) para el color/intensidad de luz ambiental
- Array `uFarolas[MAX_FAROLAS]` (vec3) con posiciones de farolas activas
- Uniforms `uFarosPos[2]`, `uFarosDir[2]` para los spotlights del vehiculo
- El fragment shader itera sobre las fuentes de luz y suma contribuciones con atenuacion

**Razon**: El fragment shader actual solo aplica `uColor` sin iluminacion. Agregar iluminacion per-fragment es el camino estandar en OpenGL 3.3 y permite efectos visuales correctos (atenuacion, conos de luz) sin cambiar la geometria.

### D4: Limite de luces en GPU

**Decision**: Definir `MAX_FAROLAS = 20` y `MAX_SEMAFOROS = 10` como constantes en el shader. Pasar el conteo real con uniforms `uFarolasCount` y `uSemaforosCount`.

**Razon**: GLSL no permite arrays dinamicos en uniforms. 20 farolas + 10 semaforos + 2 faros = 32 fuentes de luz es manejable en un solo pase sin degradar rendimiento en GPUs modernas. Se evita la complejidad de deferred rendering.

### D5: Minimapa con glViewport

**Decision**: Renderizar el minimapa en el mismo framebuffer usando un segundo pase con `glViewport` reducido (180x180 px en esquina inferior derecha) y proyeccion ortografica.

**Implementacion**:
1. Despues de dibujar la escena principal, guardar el viewport actual
2. Configurar `glViewport(ancho - 190, 10, 180, 180)`
3. No limpiar buffers (se dibuja sobre la escena)
4. Activar `uMapa = 1` en el vertex shader para proyeccion ortografica
5. Dibujar la cuadricula completa como rectangulos planos coloreados
6. Dibujar el icono del vehiculo (triangulo rotado)
7. Restaurar el viewport original

**Razon**: El vertex shader ya tiene soporte para `uMapa = 1` con proyeccion ortografica. Usar `glViewport` es la tecnica estandar para minimapas en OpenGL y no requiere framebuffers adicionales. Es eficiente porque reutiliza el mismo programa de shaders.

### D6: Semaforos con estado temporal

**Decision**: Cada semaforo tiene un acumulador de tiempo y una fase (0=verde, 1=amarillo, 2=rojo). La fase avanza automaticamente segun duraciones predefinidas (5s, 2s, 5s). Los semaforos perpendiculares se desfasan para coordinacion.

**Implementacion**: Un array de structs (o arrays paralelos en Java) con `{posX, posZ, fase, tiempoAcumulado, offsetInicial}`. En cada `actualizar(deltaTime)` se avanza el acumulador y se cambia de fase cuando corresponde.

**Razon**: Es el modelo mas simple que produce el comportamiento visual correcto. No se necesita una maquina de estados compleja porque la secuencia es fija y ciclica.

### D7: Decoracion como geometria adicional en metodos existentes

**Decision**: Los arboles, bancos, ventanas y pasos peatonales se dibujan como cajas adicionales dentro de los metodos `escena()` y helpers, usando el mismo sistema de `caja()`/`cajaGirada()`.

**Razon**: El sistema actual ya dibuja toda la escena con cajas. Agregar mas cajas para decoracion mantiene la consistencia visual y no requiere nuevos sistemas de renderizado. Las ventanas se simulan como cajas planas (profundidad minima) sobre las caras de los edificios.

### D8: Vehiculo con camara en tercera persona

**Decision**: La clase `Vehiculo` mantiene posicion (x, z), angulo y velocidad. La camara se posiciona detras y arriba del vehiculo, siguiendo su orientacion. Controles: W/S = adelante/atras, A/D = girar.

**Razon**: La camara orbital actual no es compatible con un vehiculo controlable. Una camara en tercera persona es la mas natural para conduccion urbana y permite apreciar tanto el vehiculo como el entorno.

## Risks / Trade-offs

### R1: Rendimiento con muchas luces

**Riesgo**: El fragment shader itera sobre hasta 32 fuentes de luz por cada fragmento. Con una escena de 110x110 unidades y muchos objetos, esto podria degradar el framerate en GPUs integradas.

**Mitigacion**: Limitar `MAX_FAROLAS` a 20 y usar atenuacion con corte temprano (`if (distancia > radio) continue`). Si el rendimiento es insuficiente, reducir el numero de farolas o implementar culling basico (solo procesar farolas cercanas a la camara). No se usa deferred rendering para mantener la simplicidad.

### R2: Complejidad del minimapa simultaneo

**Riesgo**: Dibujar la escena dos veces (principal + minimapa) duplica los draw calls. Con ~200+ objetos decorativos, esto podria ser costoso.

**Mitigacion**: El minimapa dibuja una version simplificada: solo rectangulos planos coloreados por tipo de celda (121 cajas planas) + 1 triangulo para el vehiculo. No dibuja decoracion, ventanas ni arboles en el minimapa. Esto limita el segundo pase a ~130 draw calls.

### R3: Z-fighting en decoracion

**Riesgo**: Los pasos peatonales y las aceras estan casi a la misma altura que el suelo, lo que puede causar z-fighting (parpadeo de superficies).

**Mitigacion**: Posicionar las superficies decorativas a Y = 0.01 o Y = 0.02 por encima del suelo. Esta separacion es imperceptible visualmente pero suficiente para evitar conflictos de profundidad.

### R4: Ventanas como geometria individual

**Riesgo**: Dibujar cada ventana como un `glDrawArrays` separado genera muchos draw calls. Un edificio con 20 ventanas por cara y 4 caras = 80 draw calls extra por edificio.

**Mitigacion**: Limitar las ventanas a 2-3 filas y 3-4 columnas por cara visible (solo caras orientadas hacia la camara si el rendimiento lo requiere). Alternativa: implementar las ventanas como variacion de color en el fragment shader usando coordenadas UV, pero esto requiere cambios mas profundos en el pipeline.

### R5: Sincronizacion de semaforos perpendiculares

**Riesgo**: Si los offsets iniciales no se calculan correctamente, dos semaforos perpendiculares en la misma interseccion podrian estar ambos en verde simultaneamente.

**Mitigacion**: Definir los pares perpendiculares explicitamente. El semaforo del eje Z tiene un offset de medio ciclo (6 segundos) respecto al del eje X en la misma interseccion. Validar en inicializacion que ningun par tenga fases compatibles simultaneas.
