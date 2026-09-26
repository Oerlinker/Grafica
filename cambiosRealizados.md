# Cambios realizados en `AppCiudad.java`

Este documento explica, paso a paso y en orden cronológico, todo lo que se hizo
en esta sesión sobre `src/main/java/com/graphics/AppCiudad.java`, partiendo del
archivo incompleto original hasta llegar al archivo actual. La idea es que si
el docente pide un cambio puedas ubicar exactamente qué método tocar y cómo,
sin tener que pedir ayuda de nuevo.

Al final del documento hay una **guía rápida de "cómo hago X"** con los cambios
más probables que te puedan pedir.

---

## 0. Estado inicial del archivo

Antes de esta sesión, `AppCiudad.java` **no compilaba ni funcionaba**. Tenía:

- Los campos, el `MAPA` (matriz de la ciudad), `run()`, `iniciar()`, `loop()`,
  `limpiar()`, el manejo de teclado (`tecla`, `pulsada`, `actualizar`), el
  cálculo de cámara (`configurarCamara`), el dibujo de la escena (`escena`,
  `dibujarMarcasCalle`, `caja`, `cajaGirada`) y los shaders (`vertexShader`,
  `fragmentShader`) ya escritos.
- **Pero le faltaban piezas clave**:
  - `crearPrograma()` y `crearCubo()` estaban **invocados** en `iniciar()`
    pero **no existían** (no compilaba).
  - No había método `main`, así que no se podía ejecutar.
  - El GLSL de los shaders tenía **dos `;` faltantes** (uno en cada shader),
    lo que hubiera hecho fallar la compilación del shader en tiempo de
    ejecución aunque el `.java` compilara.
  - En `loop()`, las variables `anchoReal`/`altoReal` se declaraban pero
    **nunca se llenaban** (faltaba `glfwGetFramebufferSize`), por lo que
    `ancho`/`alto` quedaban en 0 y `dibujarFrame()` nunca se hubiera llegado
    a ejecutar.
  - `prepararLuces()` existía pero estaba **vacío** (no mandaba ninguna luz
    al shader).
  - El campo `vistaMapa` existía pero **no se usaba en ningún lado** (el
    uniform `uMapa` se mandaba siempre en `0`).

## 1. Completar el archivo para que compile y corra

**Qué se creó:**

- `crearPrograma()` (línea ~190): compila el vertex y el fragment shader,
  los enlaza en un programa de OpenGL (`glCreateProgram`, `glAttachShader`,
  `glLinkProgram`), valida el link con `glGetProgrami(..., GL_LINK_STATUS)` y
  borra los shaders temporales con `glDeleteShader`.
- `comprobarShader(int shader, String tipo)` (línea ~215): helper que revisa
  `GL_COMPILE_STATUS` y lanza `IllegalStateException` con el log de error si
  falló la compilación.
- `crearCubo()` (línea ~222): genera un **cubo unitario** (lado 1, centrado en
  el origen) de 36 vértices (6 caras × 2 triángulos × 3 vértices, sin usar
  índices), donde cada vértice trae **posición (3 floats) + normal (3
  floats)**. Sube esos datos a un VBO y configura el VAO con dos atributos:
  - atributo `0` = posición (`aPos` en el shader)
  - atributo `1` = normal (`aNormal` en el shader)

  Esta es la geometría que reutiliza `cajaGirada()` para dibujar *todo* en la
  escena (calles, edificios, casas, autos, personas): siempre es el mismo
  cubo, solo cambia la posición/escala/color/giro que se manda como uniform
  antes de cada `glDrawArrays(GL_TRIANGLES, 0, 36)`.
- `public static void main(String[] args)` (al final de la clase): simplemente
  hace `new AppCiudad().run();` para poder ejecutar la clase.

**Qué se corrigió:**

- Los dos `;` faltantes en el GLSL:
  - En `vertexShader()`, rama del minimapa: `gl_Position = vec4(...)` → se le
    agregó el `;`.
  - En `fragmentShader()`: `color = vec4(uColor, 1.0)` → se le agregó el `;`.
- En `loop()` se agregó la línea
  `glfwGetFramebufferSize(ventana, anchoReal, altoReal);` justo antes de leer
  `anchoReal[0]`/`altoReal[0]`, para que `ancho`/`alto` reflejen el tamaño
  real del framebuffer en cada frame.
- Se agregaron los imports estáticos necesarios para todo lo anterior
  (`glCreateShader`, `glShaderSource`, `glCompileShader`, `glAttachShader`,
  `glLinkProgram`, `glGenVertexArrays`, `glGenBuffers`, `glBufferData`,
  `glVertexAttribPointer`, etc., de `GL15`/`GL20`/`GL30`) más
  `org.lwjgl.BufferUtils` y `java.nio.FloatBuffer`.

En este punto el archivo ya compilaba y la ventana debía abrir mostrando la
ciudad con la cámara orbitando con las flechas izquierda/derecha (eso ya
estaba implementado desde antes).

---

## 2. Agregar zoom, altura de cámara y minimapa

Se pidió agregar controles inspirados en `AppZoom.java` (zoom con rueda y
teclas) y `AppMovimientoTeclado.java`/`AppMovimientoZoom.java` (movimiento con
teclado), además de aprovechar el campo `vistaMapa` que ya existía sin usar.

**Campos nuevos** (cerca del inicio de la clase, junto a `orbita`):

```java
private float zoom = 1.0f;
private static final float ZOOM_MIN = 0.4f;
private static final float ZOOM_MAX = 3.0f;
private static final float VELOCIDAD_ZOOM = 1.25f;

private float alturaCamara = 55;
private static final float ALTURA_MIN = 15;
private static final float ALTURA_MAX = 120;
private static final float VELOCIDAD_ALTURA = 25f;
```

**Qué se modificó:**

- `iniciar()`: se agregó `glfwSetScrollCallback(ventana, ...)` que ajusta
  `zoom` con la rueda del mouse (`zoom += yoffset * 0.10f`, luego `clamp`).
  También se cambió el título de la ventana para listar los controles nuevos.
- `tecla(int key)`: se agregó que la tecla **M** alterna `vistaMapa`
  (`vistaMapa = !vistaMapa;`).
- `actualizar(float deltaTime)` (en ese momento todavía se llamaba así, ver
  sección 3): se agregó manejo de:
  - Flechas **arriba/abajo** → suman/restan a `alturaCamara`, luego
    `clamp(alturaCamara, ALTURA_MIN, ALTURA_MAX)`.
  - Teclas **`+`/`-`** (`GLFW_KEY_KP_ADD`/`GLFW_KEY_EQUAL` y
    `GLFW_KEY_KP_SUBTRACT`/`GLFW_KEY_MINUS`) → suman/restan a `zoom`, luego
    `clamp(zoom, ZOOM_MIN, ZOOM_MAX)`.
- Se agregó el helper `clamp(float valor, float min, float max)` (no existía
  en este archivo, sí en los otros Apps).
- `configurarCamara()`: antes calculaba el radio de la órbita fijo en `65` y
  la altura fija en `55`. Ahora:
  ```java
  float radio = 65 / zoom;          // mas zoom = camara mas cerca
  ...
  vector("uOjo", camaraX, alturaCamara, camaraZ); // altura variable
  ```
- `dibujarFrame()`: `entero("uMapa", 0)` (fijo) se cambió a
  `entero("uMapa", vistaMapa ? 1 : 0)`, conectando por fin el campo
  `vistaMapa` con la rama del vertex shader que dibuja el minimapo
  (`if (uMapa == 1) { ... }`, que ya existía en el shader pero nunca se
  activaba).

---

## 3. Alinear el estilo con los otros archivos (`procesarInput` + patrón `paso`)

Se notó que en `AppMovimientoTeclado.java`, `AppZoom.java` y
`AppMovimientoZoom.java` el método de entrada se llama **`procesarInput`**, y
el movimiento nunca se aplica multiplicando directamente por `deltaTime`, sino
guardando primero `float paso = VELOCIDAD * deltaTime;` y sumando/restando
`paso`. En `AppCiudad` el giro de la órbita todavía hacía
`orbita -= deltaTime;` directo, sin una constante de velocidad.

**Qué se modificó:**

- Se renombró el método `actualizar(float deltaTime)` a
  `procesarInput(float deltaTime)` (y su llamada en `loop()`).
- Se agregó la constante `VELOCIDAD_ORBITA = 1.0f` (mantiene la misma
  velocidad que tenía antes, porque antes equivalía a multiplicar por 1).
- El giro ahora hace:
  ```java
  float pasoOrbita = VELOCIDAD_ORBITA * deltaTime;
  if (pulsada(GLFW_KEY_LEFT))  orbita -= pasoOrbita;
  if (pulsada(GLFW_KEY_RIGHT)) orbita += pasoOrbita;
  ```
  Los otros dos movimientos (`pasoAltura`, `pasoZoom`) ya seguían ese patrón
  desde el paso 2, así que quedaron igual.

**`prepararLuces()`** (estaba vacío desde el archivo original):

Se implementó para mandar una luz solar fija (direccional) al shader:

```java
private void prepararLuces() {
    vector("uLuzDir", 0.35f, 0.8f, 0.45f);
    vector("uLuzColor", 1.0f, 0.96f, 0.88f);
    vector("uAmbiente", 0.35f, 0.35f, 0.38f);
}
```

Para que esto sirviera de algo hubo que **tocar el fragment shader**, que
antes solo pintaba `uColor` plano y no usaba `vNormal` (que el vertex shader
ya calculaba pero nadie leía). Se le agregaron los uniforms `uLuzDir`,
`uLuzColor`, `uAmbiente` y un cálculo de iluminación difusa muy simple:

```glsl
in vec3 vNormal;
uniform vec3 uColor;
uniform vec3 uLuzDir;
uniform vec3 uLuzColor;
uniform vec3 uAmbiente;
out vec4 color;
void main() {
    vec3 normal = normalize(vNormal);
    float difusa = max(dot(normal, normalize(uLuzDir)), 0.0);
    vec3 iluminacion = uAmbiente + uLuzColor * difusa;
    color = vec4(uColor * iluminacion, 1.0);
}
```

Como **todo** en la escena se dibuja con el mismo programa/shader (llamando a
`caja()`/`cajaGirada()`), esta iluminación se aplica automáticamente a
cualquier cosa nueva que se dibuje así (por eso el auto y las personas del
siguiente paso "ya vienen iluminados" sin tocar nada más).

---

## 4. Agregar un auto y personas

**Campos nuevos:**

```java
private float autoPosicion = -LIMITE;
private static final float VELOCIDAD_AUTO = 6f;
```

**Qué se modificó:**

- `procesarInput()`: al final se agregó
  ```java
  autoPosicion += VELOCIDAD_AUTO * deltaTime;
  if (autoPosicion > LIMITE) {
      autoPosicion = -LIMITE; // reaparece en el otro extremo de la calle
  }
  ```
- `escena()`: al final (después de dibujar toda la cuadrícula) se agregó la
  llamada `dibujarAuto(autoPosicion, centro(2));` — el auto recorre la calle
  horizontal de la **fila 2** del `MAPA` (esa fila es toda `0`, o sea calle).

**Métodos nuevos:**

- `dibujarAuto(float x, float z)`: arma un auto **solo con cajas**:
  carrocería roja, cabina/vidrios más chica encima, y 4 llantas oscuras en las
  esquinas. Todo hecho con llamadas a `caja(...)`.
- `dibujarPersona(float x, float z, float r, float g, float b)`: arma una
  persona con **dos cajas**: cuerpo (color parametrizable por `r,g,b`, para
  poder pintar "ropa" distinta) y cabeza (color piel fijo).

Las personas se llaman desde `dibujarParque(...)` (ver sección 5), dos por
cada celda de parque, con colores de ropa distintos.

---

## 5. Corregir el manejo de los 4 tipos de celda del mapa

El `MAPA` es una matriz `7x7` donde cada número representa un tipo de celda:

```java
private static final int[][] MAPA = {
        { 0, 0, 0, 0, 0, 0, 0 },
        { 0, 1, 0, 1, 0, 1, 0 },
        { 0, 0, 0, 0, 0, 0, 0 },
        { 0, 2, 0, 1, 0, 1, 0 },
        { 0, 0, 0, 0, 0, 0, 0 },
        { 0, 1, 0, 3, 0, 1, 0 },
        { 0, 0, 0, 0, 0, 0, 0 }
};
// 0 = calle, 1 = manzana, 2 = parque, 3 = edificio
```

**Antes del arreglo**, el código solo distinguía `tipo == 0` (calle) de
`tipo == 1` (que dibujaba una torre alta) y **todo lo demás** (incluido un
futuro `3`) se dibujaba como parque. Al cambiar la celda `[5][3]` de `2` a
`3` (para que fuera un edificio), en realidad seguía dibujándose como parque,
porque no existía una rama específica para `3`.

**Qué se modificó — `escena()`:**

El `if/else` se cambió por un `switch` explícito sobre los 4 tipos, con un
`default` que **lanza una excepción** si aparece un número no contemplado (útil
para detectar errores de tipeo en el `MAPA` en vez de dibujar algo
silenciosamente incorrecto):

```java
switch (tipo) {
    case 0 -> dibujarMarcasCalle(fila, columna, x, z);
    case 1 -> { caja(...base gris...); dibujarManzana(x, z, fila, columna); }
    case 2 -> { caja(...base gris...); dibujarParque(x, z); }
    case 3 -> { caja(...base gris...); dibujarEdificio(x, z, fila, columna); }
    default -> throw new IllegalStateException("Tipo de celda desconocido: " + tipo);
}
```

**Métodos nuevos (extraídos/creados a partir del código viejo):**

- `dibujarManzana(float x, float z, int fila, int columna)` — **nuevo**,
  para `tipo == 1`. En vez de una sola torre, dibuja **4 casas bajas**
  (altura entre 2.5 y 4.5, variando con `(fila + columna + i) % 3`) repartidas
  en las 4 esquinas de la celda, cada una con un "techo" (una caja plana
  encima, color ladrillo).
- `dibujarParque(float x, float z)` — **extraído** del viejo bloque `else`
  del `if (tipo == 1)`. Dibuja el piso verde y llama dos veces a
  `dibujarPersona(...)`. Ahora corresponde a `tipo == 2`.
- `dibujarEdificio(float x, float z, int fila, int columna)` — **extraído**
  tal cual del viejo bloque `if (tipo == 1)` (la torre alta con la fórmula de
  altura pseudoaleatoria `5 + (fila*3 + columna*7) % 9`). Ahora corresponde a
  `tipo == 3`.

---

## 6. Estructura final del archivo (mapa de métodos)

Orden real en el archivo, de arriba hacia abajo:

1. **Campos**: `ventana`, `programa`, `vao`, `vbo`, `ancho`, `alto`, `orbita`,
   `vistaMapa`, `LIMITE`, `CELDA`, `zoom` (+min/max/velocidad),
   `alturaCamara` (+min/max/velocidad), `VELOCIDAD_ORBITA`, `autoPosicion`,
   `VELOCIDAD_AUTO`, `uniforms` (caché de ubicaciones de uniforms), `MAPA`.
2. `run()` — orquesta `iniciar()` → `loop()` → `limpiar()` con try/finally.
3. `iniciar()` — crea ventana GLFW, registra callbacks de teclado y rueda del
   mouse, habilita depth test, llama a `crearPrograma()` y `crearCubo()`.
4. `crearPrograma()` / `comprobarShader()` — compilan y enlazan los shaders.
5. `crearCubo()` — genera la geometría del cubo (VAO/VBO) que se reutiliza
   para dibujar todo.
6. `loop()` — bucle principal: poll de eventos, `deltaTime`, `procesarInput`,
   tamaño real del framebuffer, `dibujarFrame()`, swap de buffers.
7. `limpiar()` — libera VAO/VBO/programa/ventana y termina GLFW.
8. **Teclas y cámara**: `tecla()`, `pulsada()`, `clamp()`, `procesarInput()`,
   `configurarCamara()`, `prepararLuces()`.
9. **Ciudad a partir de la matriz**: `centro()`, `dibujarFrame()`,
   `escena()`, `dibujarManzana()`, `dibujarParque()`, `dibujarEdificio()`,
   `dibujarAuto()`, `dibujarPersona()`, `dibujarMarcasCalle()`.
10. **Dibujar cajas y enviar uniforms**: `caja()`, `cajaGirada()`,
    `uniform()`, `vector()`, `decimal()`, `entero()`.
11. **Shaders**: `vertexShader()`, `fragmentShader()` (strings GLSL).
12. `main()`.

---

## 7. Controles actuales de la aplicación

| Tecla / acción            | Efecto                                            |
|----------------------------|----------------------------------------------------|
| Flecha izquierda / derecha | Orbita la cámara alrededor de la ciudad            |
| Flecha arriba / abajo      | Sube / baja la altura de la cámara                 |
| Rueda del mouse            | Zoom in / out                                      |
| `+` / `-`                  | Zoom in / out (alternativa por teclado)            |
| `M`                        | Alterna entre vista 3D normal y minimapa           |
| `Esc`                      | Cierra la ventana                                  |

---

## 8. Guía rápida: cómo hacer cambios comunes manualmente

### Cambiar el tamaño o el diseño de la ciudad
- Editar la matriz `MAPA` (cerca del inicio de la clase). Debe seguir siendo
  **rectangular** (todas las filas del mismo largo). Los valores válidos son
  `0` (calle), `1` (manzana), `2` (parque), `3` (edificio) — cualquier otro
  número hará que la app lance `IllegalStateException` al dibujar (a
  propósito, para detectar el error).
- Si agregas filas/columnas, no hace falta tocar nada más: `escena()` recorre
  `MAPA.length` y `MAPA[fila].length` dinámicamente.
- El tamaño de cada celda es `CELDA = 10`; el borde del "piso" está en
  `LIMITE = 35`. Si agrandas mucho el mapa y las celdas se salen del piso
  gris, sube `LIMITE` o el tamaño del piso en `escena()`
  (`caja(0, -0.25f, 0, 70, 0.5f, 70, ...)`, los `70` son ancho/profundidad).

### Agregar un nuevo tipo de celda (por ejemplo `4 = agua` o `4 = plaza`)
1. Crear un método nuevo `dibujarLoQueSea(float x, float z, ...)` siguiendo el
   estilo de `dibujarParque`/`dibujarManzana`/`dibujarEdificio`.
2. Agregar un `case 4 -> { ... dibujarLoQueSea(x, z); }` dentro del `switch`
   en `escena()`.
3. Usar `caja(x, y, z, sx, sy, sz, r, g, b)` para cada bloque que quieras
   dibujar (`x,y,z` = centro; `sx,sy,sz` = tamaño en cada eje; `r,g,b` en
   rango 0–1). Si necesitas rotarlo, usa `cajaGirada(..., angulo)` (radianes).

### Cambiar colores (edificios, casas, parque, auto, personas)
- Todos los colores se pasan como los últimos 3 parámetros `r, g, b` (0.0 a
  1.0) de cada llamada a `caja(...)`. Busca el método correspondiente
  (`dibujarEdificio`, `dibujarManzana`, `dibujarParque`, `dibujarAuto`,
  `dibujarPersona`) y cambia esos números.

### Cambiar la velocidad de la cámara / el zoom / la órbita
- Son constantes al inicio de la clase: `VELOCIDAD_ORBITA`,
  `VELOCIDAD_ALTURA`, `VELOCIDAD_ZOOM`. Súbelas para que reaccione más rápido,
  bájalas para que sea más lento.
- Los límites (`ZOOM_MIN/MAX`, `ALTURA_MIN/MAX`) también son constantes ahí
  mismo.
- La distancia base de la órbita (cuando `zoom == 1`) es el `65` dentro de
  `configurarCamara()` (`float radio = 65 / zoom;`).

### Cambiar la velocidad o la ruta del auto
- `VELOCIDAD_AUTO` (constante) controla qué tan rápido recorre la calle.
- En `escena()`, la línea `dibujarAuto(autoPosicion, centro(2));` usa
  `centro(2)` como la fila fija (Z) por la que circula. Cambia el `2` por
  cualquier índice de fila que sea calle (en este mapa: 0, 2, 4 o 6) para que
  el auto circule por otra calle horizontal.
- Si quieres que circule en vertical (por una columna) en vez de horizontal,
  tendrías que invertir la lógica: animar el eje Z en vez de X, y fijar la X
  con `centro(columna)`.

### Agregar más autos o más personas
- Para más autos: agrega otro campo `float autoPosicion2 = ...;`, actualízalo
  en `procesarInput()` igual que `autoPosicion`, y agrega otra llamada a
  `dibujarAuto(autoPosicion2, centro(otraFila));` en `escena()`.
- Para más personas: agrega más llamadas a `dibujarPersona(x, z, r, g, b)`
  dentro de `dibujarParque(...)` (o donde quieras), con distintos
  desplazamientos de `x`/`z` para que no se encimen.

### Cambiar la luz (dirección, color, intensidad)
- Editar `prepararLuces()`:
  - `uLuzDir` (vec3): hacia dónde apunta la luz (no hace falta normalizar, el
    shader ya lo hace).
  - `uLuzColor` (vec3): color/intensidad de la luz directa.
  - `uAmbiente` (vec3): color/intensidad de la luz base que ilumina todo por
    igual (evita que las caras "de sombra" queden completamente negras).
- Si quieres un modelo de luz distinto (por ejemplo con especular, o luz
  puntual en vez de direccional), hay que modificar el `fragmentShader()`
  (string GLSL al final de la clase), no solo `prepararLuces()`.

### Activar/desactivar el minimapa desde código (no solo con la tecla M)
- Cambiar el valor inicial del campo `private boolean vistaMapa = false;` a
  `true`.

### Agregar una tecla nueva
1. Si es una acción de "una sola vez" (como `M` o `Esc`), agrégala dentro de
   `tecla(int key)`.
2. Si es una acción continua mientras se mantiene apretada (como orbitar o
   zoom), agrégala dentro de `procesarInput(float deltaTime)` usando
   `pulsada(GLFW_KEY_XXX)`, siguiendo el patrón
   `float paso = VELOCIDAD_X * deltaTime;` y sumando/restando `paso` (no
   `deltaTime` directo).

### Recompilar y probar sin Maven en el PATH
Si `mvn` no está disponible en la terminal, se puede compilar el archivo
suelto contra los `.jar` de LWJGL del repositorio local de Maven
(`~/.m2/repository`) con PowerShell:

```powershell
$jars = (Get-ChildItem -Path "$env:USERPROFILE\.m2\repository" -Recurse -Filter "*.jar" |
    Where-Object { $_.Name -notmatch "sources|javadoc" -and $_.Name -match "lwjgl" }).FullName -join ";"
javac -cp $jars -d target\classes src\main\java\com\graphics\AppCiudad.java
```

Esto solo verifica que el `.java` compila; para **ejecutarlo** (y ver la
ventana) es mejor usar Maven o correrlo desde el IDE, porque además del
classpath hace falta que LWJGL cargue las librerías nativas (`lwjgl natives`)
correspondientes al sistema operativo.
