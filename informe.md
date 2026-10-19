# Documentación del proyecto: Ciudad 3D con vehículo (OpenGL / LWJGL)

Oct 9, 2026 · @Andres Antonio Segovia Ampuero

## 1. Descripción general

El proyecto es una ciudad 3D en tiempo real, escrita en Java con LWJGL 3 (GLFW + OpenGL 3.3 core), en la que el usuario conduce un auto entre edificios, parques, semáforos y tráfico automático.

El código se reparte en dos clases del paquete `com.graphics`:

| Clase | Rol |
|---|---|
| `AppCiudad` | Motor y escena: ventana, ciclo de render, shaders, mapa, edificios, luces, semáforos, autos NPC, minimapa y cámara orbital. |
| `Vehiculo` | Extiende `AppCiudad`. Agrega la conducción del auto del jugador, las colisiones y la cámara de persecución. Es la clase que se ejecuta normalmente. |

Todo se dibuja con un único cubo unitario (36 vértices con normales) que se posiciona, escala, gira en Y y colorea mediante uniforms. No se usan texturas, modelos ni archivos externos.

## 2. Diseño de la ciudad

La ciudad es una cuadrícula de 11 × 11 celdas de 10 unidades (110 × 110 unidades en total, límite ±55), definida en la matriz `MAPA`. Las filas y columnas pares son calles; las impares cruzadas son manzanas.

```
  col: 0 1 2 3 4 5 6 7 8 9 10
f0 :  . . . . . . . . . . .
f1 :  . A . A . P . B . A .
f2 :  . . . . . . . . . . .
f3 :  . B . A . A . A . P .
f4 :  . . . . . . . . . . .
f5 :  . A . P . A . B . A .
f6 :  . . . . . . . . . . .
f7 :  . A . B . A . A . B .
f8 :  . . . . . . . . . . .
f9 :  . P . A . B . A . A .
f10:  . . . . . . . . . . .
```

`. = calle (0)` · `A = edificio alto (1)` · `B = edificio bajo (2)` · `P = parque (3)`

La cuadrícula tiene 25 manzanas: 15 edificios altos, 6 bajos y 4 parques, separadas por 6 calles horizontales y 6 verticales (36 intersecciones).

### Elementos de la escena

| Elemento | Cómo se construye |
|---|---|
| Suelo | Caja oscura de 110 × 110 bajo toda la ciudad. |
| Manzanas | Losa gris de 10 × 10 que hace de acera. |
| Edificios altos (1) | Caja de 7 × 7, altura de 5 a 13 según fila y columna, con techo ligeramente más ancho. |
| Edificios bajos (2) | Igual, con altura de 3 a 6. El color varía con la columna y la fila. |
| Ventanas | Hasta 6 pisos, 3 ventanas por piso y por cara. De noche, cerca de la mitad se ilumina (patrón fijo por edificio). |
| Parques (3) | Césped de 9 × 9, 2 a 4 árboles (tronco y copa en cajas) y 1 o 2 bancos girados. Posiciones derivadas de la fila y la columna, no aleatorias. |
| Calles | Líneas amarillas discontinuas en los tramos entre intersecciones. |
| Pasos peatonales | 5 franjas blancas en las intersecciones, del lado de cada manzana vecina. |
| Farolas | 18 postes de 5 unidades en las intersecciones donde (fila + columna) es múltiplo de 4. |
| Semáforos | 8 en intersecciones fijas. Ciclo de 12 s: verde 5 s, amarillo 2 s, rojo 5 s, con fases desfasadas entre ellos. |
| Autos NPC | 8 autos de colores distintos que recorren las calles a 6, 8 o 10 unidades por segundo. |
| Minimapa | Vista cenital de 180 × 180 px en la esquina inferior derecha, con las manzanas por tipo y el auto del jugador en amarillo. |

### Iluminación y ciclo día/noche

La luz se calcula en el fragment shader sumando varias fuentes sobre una luz ambiental:

- **Ambiente y color del cielo:** cambian gradualmente entre día y noche (transición de unos 4 s al pulsar N).
- **Difusa:** una dirección fija (0.3, 1.0, 0.2), más débil de noche.
- **Farolas:** halo cálido de radio 10, solo de noche.
- **Semáforos:** halo del color de su fase, radio 5, solo de noche.
- **Faros del auto:** dos conos de 30° y alcance 20, activables con F.

### Comportamiento de los NPC

Cada NPC avanza en una de las 4 direcciones de la cuadrícula. Al llegar al centro de una intersección elige una dirección válida al azar (sin retroceder, salvo que no haya otra salida). Frena si hay un semáforo en contra, si el auto del jugador o otro NPC está justo adelante. Al salir del mapa reaparece del lado opuesto.

### Chata (remolque de equipaje)

La chata es una extensión para equipaje que se acopla a la parte trasera del vehículo como una pieza separada. Se activa y desactiva con la tecla E.

**Diseño visual:**

- Plataforma plana gris de 1.6 × 2.2 unidades con barandas laterales y trasera.
- Dos ruedas propias en la parte trasera de la chata.
- Barra de enganche visible que conecta la trasera del auto con el frente de la chata.

**Física del remolque:**

- La chata es una pieza independiente con su propia posición y ángulo, conectada al auto por un punto de enganche a 2.0 unidades detrás del centro del vehículo.
- El ángulo de giro está limitado a ±0.6 radianes (~34°) respecto al ángulo del auto. Esto impide que la chata gire 360° libremente y simula el comportamiento de un enganche real.
- La chata tiene colisión propia: se verifican 3 puntos a lo largo de su largo (frente, centro y atrás) contra manzanas, límites del mapa y NPCs, con un radio de colisión de 1.4 unidades.
- Si la chata colisiona, el vehículo frena (la velocidad se reduce al 30%), simulando la resistencia del remolque al impactar.
- Al activar la chata con E, se inicializa alineada detrás del auto. Al reiniciar con R, se desacopla.

**Funciones involucradas:** `inicializarChata()`, `actualizarChata()`, `chataColisiona()`, `dibujarVehiculo()` (override).

## 3. Controles

Al ejecutar `Vehiculo` se usan los controles de conducción y los de la ciudad. Al ejecutar `AppCiudad` solo los de la ciudad.

| Tecla / entrada | Acción | Definido en |
|---|---|---|
| W o ↑ | Acelerar | `Vehiculo` |
| S o ↓ | Frenar / reversa | `Vehiculo` |
| A o ← | Girar a la izquierda | `Vehiculo` |
| D o → | Girar a la derecha | `Vehiculo` |
| Espacio | Freno fuerte | `Vehiculo` |
| F | Encender o apagar los faros | `Vehiculo` |
| C | Alternar cámara de persecución y cámara orbital | `Vehiculo` |
| R | Reiniciar el auto (posición, velocidad, faros y modo día) | `Vehiculo` |
| E | Acoplar o desacoplar la chata (remolque de equipaje) | `Vehiculo` |
| N | Alternar día y noche | `AppCiudad` |
| M | Mostrar u ocultar el minimapa | `AppCiudad` |
| Esc | Salir | `AppCiudad` |
| ← / → (cámara orbital) | Orbitar alrededor de la ciudad | `AppCiudad` |
| ↑ / ↓ (cámara orbital) | Subir o bajar la altura de la cámara (15 a 120) | `AppCiudad` |
| + / − o rueda del mouse | Acercar o alejar (zoom 0.4 a 3.0) | `AppCiudad` |

La barra de título de la ventana muestra la velocidad en km/h (velocidad interna × 3.6) y un resumen de las teclas.

Las flechas hacen dos cosas a la vez en `Vehiculo`: conducen el auto y mueven la cámara orbital. Para conducir sin efectos secundarios conviene usar WASD.

## 4. Funciones

Cada fotograma sigue este orden: `loop()` calcula el deltaTime (máximo 0.05 s), llama a `actualizar()` y luego a `dibujarFrame()`.

### Clase AppCiudad

| Función | Qué hace |
|---|---|
| `run()`, `iniciar()`, `loop()`, `limpiar()` | Ciclo de vida: crea la ventana de 1100 × 760 con contexto OpenGL 3.3 core, registra teclado y rueda, inicializa semáforos, NPC, shaders y cubo, ejecuta el bucle y libera recursos. La propiedad `-Ddemo.frames=N` cierra la app tras N cuadros. |
| `tecla(int)` | Maneja Esc, N y M. Se sobrescribe en `Vehiculo`. |
| `actualizar(float)` | Procesa cámara orbital y zoom, la transición día/noche, los NPC y el ciclo de los semáforos. |
| `configurarCamara()` | Cámara orbital: radio 90 / zoom, altura variable, mirando al origen. |
| `prepararLuces()` | Envía al shader la luz ambiente, farolas, faros y semáforos (con el color de su fase actual). |
| `dibujarFrame()` | Limpia pantalla con el color del cielo, activa el programa y dibuja escena, auto, farolas, semáforos, NPC y minimapa. |
| `escena()` | Recorre `MAPA` y dibuja suelo, manzanas, edificios, parques, marcas de calle y pasos peatonales. |
| `dibujarVentanas()`, `dibujarArbolesYBancos()`, `dibujarMarcasCalle()`, `dibujarPasosPeatonales()` | Detalles de la escena, con patrones calculados a partir de fila y columna. |
| `dibujarVehiculo()`, `dibujarFarolas()`, `dibujarSemaforos()`, `dibujarAutosNPC()` | Dibujan cada tipo de objeto con combinaciones de cajas. |
| `calcularFarolas()`, `inicializarSemaforos()`, `inicializarNPCs()` | Calculan posiciones iniciales, fases y colores. |
| `actualizarNPCs(float)`, `direccionesValidas()`, `debeFrenarPorSemaforo()` | Movimiento, elección de ruta y frenado de los NPC. |
| `dibujarMinimapa()` | Cambia el viewport a 180 × 180 px y activa `uMapa = 1` para proyectar en vista cenital. |
| `caja()`, `cajaGirada()` | Primitiva de dibujo: envía posición, escala, color y giro y llama a `glDrawArrays` con 36 vértices. |
| `uniform()`, `vector()`, `decimal()`, `entero()` | Ayudantes de uniforms, con caché de ubicaciones en un HashMap. |
| `vertexShader()`, `fragmentShader()`, `compilar()`, `crearPrograma()` | Shaders GLSL 330 escritos como bloques de texto; compilación y enlace con verificación de errores. |
| `crearCubo()` | Crea el VAO y VBO del cubo unitario (posición y normal por vértice). |

### Clase Vehiculo

| Función | Qué hace |
|---|---|
| `actualizar(float)` | Lee el teclado, calcula la velocidad (aceleración 20, límites de −6 a 16, resistencia exponencial de 0.7, o 7 con freno) y el giro, que depende de la velocidad. Mueve el auto solo si no hay colisión. |
| `puedeCircular(x, z)` | Comprueba si la posición siguiente es válida: dentro del límite del mapa, sin chocar con manzanas (círculo de radio 1.65 contra rectángulos) ni con NPC (radio 1.8). |
| `configurarCamara()` | Cámara de persecución a 12 unidades detrás del auto y 9 de altura, o la orbital si C está activo. |
| `reiniciar()` | Devuelve el auto a (0, −50) con velocidad 0, faros apagados y modo día. |
| `tecla(int)` | Agrega F, C, E y R a las teclas heredadas. |
| `actualizarTitulo()`, `estadoExtra()` | Muestra velocidad y ayuda en el título. `estadoExtra()` indica si la chata está activa. |
| `inicializarChata()` | Posiciona la chata alineada detrás del auto al activarla o reiniciar. |
| `actualizarChata()` | Física del remolque: calcula el ángulo de la chata hacia el punto de enganche, limita la rotación a ±34° respecto al auto y verifica colisiones. |
| `chataColisiona(x, z)` | Comprueba si la chata colisiona con manzanas, límites del mapa o NPC, evaluando 3 puntos a lo largo de su largo. |
| `dibujarVehiculo()` | Sobrescribe el método de `AppCiudad` para agregar el dibujo de la chata: plataforma, barandas, ruedas y barra de enganche. |

## 5. Limitaciones

El proyecto es una demostración de gráficos básicos, y varias decisiones de diseño lo limitan de forma consciente o por simplicidad.

**Gráficos**

- Solo hay cubos: sin texturas, modelos 3D, ruedas redondas ni curvas.
- No hay sombras, reflejos ni brillo especular. Las luces puntuales se aproximan con una atenuación lineal por distancia y no usan la normal de la superficie.
- El shader admite como máximo 20 farolas, 10 semáforos y 2 faros.
- Cada objeto es una llamada de dibujo independiente, sin instancing ni agrupación. Con más edificios o NPC, el rendimiento bajaría.
- Las ventanas de los edificios cambian de día a noche de golpe al pasar el 50 % de la transición, no de forma gradual.

**Simulación**

- Los NPC no conocen la geometría de las manzanas: solo siguen la cuadrícula. Se detienen ante cualquier obstáculo adelante y pueden bloquearse entre sí o con el jugador.
- Al salir del mapa, los NPC reaparecen en el borde opuesto (teletransporte).
- Los semáforos solo afectan a los NPC; el jugador puede ignorarlos.
- El auto del jugador atraviesa farolas y semáforos, porque la colisión solo contempla manzanas, límites del mapa y NPC.
- Los NPC no esquivan al jugador, solo se detienen frente a él.
- La física es simple: sin inercia lateral, derrapes ni daño.
- R no reinicia a los NPC ni los semáforos.

**Interfaz**

- Las flechas controlan a la vez el auto y la cámara orbital.
- El minimapa muestra al jugador, pero no a los NPC ni a los semáforos.
- No hay menú ni pantalla de ayuda: los controles se leen en el título de la ventana.

**Técnicas**

- Requiere Java con soporte de bloques de texto (JDK 15 o superior) y una GPU con OpenGL 3.3.
- La ventana se crea con compatibilidad hacia adelante para macOS, pero no se ha probado en este análisis.
- Hay código sin uso: la función `dist()` y las constantes `MAX_FAROLAS` y `MAX_SEMAFOROS` de Java (el límite real está en el shader).
- El proyecto no incluye pruebas automáticas en los archivos entregados.

## 6. Instrucciones de ejecución

Los archivos entregados son solo `AppCiudad.java` y `Vehiculo.java`; no incluyen el archivo de construcción (por ejemplo `pom.xml` o `build.gradle`). Los pasos siguientes son los habituales para un proyecto LWJGL 3 y deben ajustarse a la configuración real del proyecto.

### Requisitos

- JDK 15 o superior (el código usa bloques de texto `"""`). Se recomienda JDK 17 o 21.
- GPU y controladores con soporte de OpenGL 3.3.
- LWJGL 3 con los módulos `lwjgl`, `lwjgl-glfw` y `lwjgl-opengl`, y sus librerías nativas para el sistema operativo.
- Maven o Gradle, o un IDE (IntelliJ IDEA, Eclipse, VS Code) que gestione las dependencias.

### Pasos

1. Crear un proyecto Java y copiar los dos archivos en `src/main/java/com/graphics/`, respetando el paquete `com.graphics`.
2. Agregar las dependencias de LWJGL. La forma más simple es generar el fragmento de Maven o Gradle desde el configurador de la página oficial de LWJGL, eligiendo GLFW y OpenGL.
3. Compilar el proyecto (`mvn compile` o `gradle build`).
4. Ejecutar la clase principal `com.graphics.Vehiculo`. Para ver solo la ciudad sin conducir, ejecutar `com.graphics.AppCiudad`.

Ejemplo con Maven, si el `pom.xml` incluye el plugin `exec-maven-plugin`:

```
mvn compile exec:java -Dexec.mainClass=com.graphics.Vehiculo
```

### Ejecución en IntelliJ IDEA

1. Abrir el proyecto y comprobar en File → Project Structure que el SDK sea JDK 15 o superior y que el nivel de lenguaje coincida.
2. Verificar que las dependencias de LWJGL estén cargadas (si el proyecto usa Maven o Gradle, recargar con Sync). Sin ellas aparecen errores en los `import org.lwjgl...`.
3. Abrir `Vehiculo.java` y pulsar el triángulo verde junto a `main` → Run 'Vehiculo.main()'. IntelliJ crea la configuración de ejecución automáticamente.
4. Para ejecutar solo la ciudad, hacer lo mismo desde `AppCiudad.java`.
5. Opciones extra (por ejemplo `-Ddemo.frames=300` o `-XstartOnFirstThread` en macOS): Run → Edit Configurations → Modify options → Add VM options.

### Notas por plataforma

- **macOS:** GLFW exige que el hilo principal sea el primero de la JVM; se agrega la opción `-XstartOnFirstThread` a los argumentos de la JVM.
- **Modo de prueba:** `-Ddemo.frames=300` cierra la aplicación automáticamente tras 300 cuadros, útil para comprobar que arranca.
- Si aparece el error "No se pudo inicializar GLFW" o falla la creación del contexto, se debe actualizar el controlador de video.

## 7. Recursos y fragmentos externos

El código entregado no carga ningún archivo externo: no hay texturas, modelos, fuentes, sonidos ni shaders en archivos aparte. Toda la geometría y los shaders están dentro de los dos archivos `.java`.

| Recurso | Uso en el proyecto | Licencia |
|---|---|---|
| LWJGL 3 | Enlaces Java a GLFW y OpenGL (`org.lwjgl.glfw`, `org.lwjgl.opengl`). | BSD de 3 cláusulas |
| GLFW | Ventana, contexto OpenGL, teclado y rueda del mouse (vía LWJGL). | zlib/libpng |
| OpenGL 3.3 core / GLSL 330 | API gráfica y lenguaje de los shaders. | Especificación del Grupo Khronos |
| JDK estándar | `HashMap`, `ArrayList`, `Math` y bloques de texto. | Según la distribución del JDK usada |

### Técnicas estándar reimplementadas

Estas técnicas son de uso común y están escritas en el propio código, sin copiar una biblioteca:

- Matriz de vista y proyección en perspectiva calculadas a mano en el vertex shader (campo de visión de 55°, planos cercano 0.1 y lejano 250).
- Cubo unitario de 36 vértices con normales por cara.
- Atenuación lineal de luces puntuales y de cono, y colisión círculo contra rectángulo.
