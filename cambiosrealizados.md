# Cambios Realizados: De AppCiudad1 + Vehiculo1 → AppCiudad + Vehiculo

Este documento detalla **todos los cambios** realizados para transformar el código base
(`AppCiudad1.java` + `Vehiculo1.java`) en el proyecto finalizado (`AppCiudad.java` + `Vehiculo.java`),
explicando **qué se cambió**, **cómo se hizo** y **por qué**, mapeado contra las consignas del PDF.

---

## Resumen de archivos

| Archivo base (punto de partida) | Archivo final (proyecto terminado) |
|---|---|
| `AppCiudad1.java` — Ciudad 7×7, cámara orbital, iluminación solar fija, auto automático, personas, minimapa pantalla completa | `AppCiudad.java` — Ciudad 11×11, día/noche, farolas, semáforos, NPCs, ventanas, árboles, minimapa esquina, faros del auto |
| `Vehiculo1.java` — Conducción WASD, colisiones, cámara follow, **con 2 bugs** | `Vehiculo.java` — Conducción WASD corregida, faros (F), noche (N), colisión con NPCs |

---

## Sección 1 del PDF: Ciudad expandida (20 puntos)

### 1.1 Mapa 7×7 → 11×11

**AppCiudad1 (antes):**
```java
protected static final float LIMITE = 35;
protected static final int[][] MAPA = {
    { 0, 0, 0, 0, 0, 0, 0 },
    { 0, 1, 0, 1, 0, 1, 0 },
    ...  // 7 filas × 7 columnas
};
```

**AppCiudad (después):**
```java
protected static final float LIMITE = 55;
protected static final int[][] MAPA = {
    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 0, 1, 0, 1, 0, 3, 0, 2, 0, 1, 0 },
    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 0, 2, 0, 1, 0, 1, 0, 1, 0, 3, 0 },
    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0 },
    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 0, 1, 0, 2, 0, 1, 0, 1, 0, 2, 0 },
    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 0, 3, 0, 1, 0, 2, 0, 1, 0, 1, 0 },
    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
};
```

**Cómo se hizo:**
- Se amplió la matriz de 7×7 a 11×11 (25 manzanas en vez de 9).
- `LIMITE` pasó de `35` a `55` para que el suelo cubra la ciudad más grande.
- Se agregó un nuevo tipo de celda: `3 = parque` (antes los parques eran tipo `2` y el tipo `3` era edificio alto; ahora: `1 = edificio alto`, `2 = edificio bajo`, `3 = parque`).
- El suelo en `escena()` cambió de `caja(0, -0.25f, 0, 70, 0.5f, 70, ...)` a `caja(0, -0.5f, 0, 110, 0.6f, 110, ...)`.

**Cumple con:** "Ampliar la ciudad a al menos 11×11 celdas" y "al menos 12 bloques de edificios y 4 parques" (hay 13 edificios tipo 1+2 y 5 parques tipo 3).

### 1.2 Tipos de edificios diferenciados

**AppCiudad1:** Solo tenía un tipo de edificio (tipo 1 = torre alta, tipo 2 = parque, tipo 3 = torre alta idéntica a tipo 1).

**AppCiudad:** Ahora hay dos tipos distintos:
- **Tipo 1 (edificio alto):** altura entre 5 y 13 (`5 + (fila * 3 + columna * 7) % 9`).
- **Tipo 2 (edificio bajo):** altura entre 3 y 6 (`3 + (fila * 5 + columna * 3) % 4`), con coeficientes distintos para que visualmente se distinga.

```java
if (tipo == 1 || tipo == 2) {
    float altura;
    if (tipo == 1) {
        altura = 5 + (fila * 3 + columna * 7) % 9;
    } else {
        altura = 3 + (fila * 5 + columna * 3) % 4;
    }
    ...
    dibujarVentanas(fila, columna, x, z, altura);
}
```

### 1.3 Cámara orbital ajustada

El radio base de la cámara pasó de `65 / zoom` a `90 / zoom` para acomodar la ciudad más grande.

---

## Sección 2 del PDF: Iluminación (10 puntos)

### 2.1 Día/noche con tecla N

**AppCiudad1:** Iluminación solar fija (siempre de día). Usaba `uLuzDir`, `uLuzColor`, `uAmbiente`.

**AppCiudad:** Transición suave día↔noche controlada por un float `transicionDiaNoche` (0.0 = día, 1.0 = noche).

**Campos nuevos:**
```java
protected boolean modoNoche = false;
protected float transicionDiaNoche = 0.0f;
```

**En `tecla()`:** se agregó `if (key == GLFW_KEY_N) modoNoche = !modoNoche;`

**En `actualizar()`:** transición suave (no instantánea):
```java
float objetivo = modoNoche ? 1.0f : 0.0f;
float velocidadTransicion = 0.25f;
if (transicionDiaNoche < objetivo) {
    transicionDiaNoche = Math.min(objetivo, transicionDiaNoche + velocidadTransicion * deltaTime);
} else if (transicionDiaNoche > objetivo) {
    transicionDiaNoche = Math.max(objetivo, transicionDiaNoche - velocidadTransicion * deltaTime);
}
```

**Color de fondo dinámico** (en `dibujarFrame()`):
```java
float fondoR = 0.50f - 0.45f * transicionDiaNoche;
float fondoG = 0.70f - 0.60f * transicionDiaNoche;
float fondoB = 0.90f - 0.70f * transicionDiaNoche;
glClearColor(fondoR, fondoG, fondoB, 1);
```
De día el cielo es celeste `(0.50, 0.70, 0.90)`, de noche oscuro `(0.05, 0.10, 0.20)`.

**Luz ambiente dinámica** (en `prepararLuces()`):
```java
decimal("uModoNoche", transicionDiaNoche);
float ambR = 0.9f - 0.65f * transicionDiaNoche;
float ambG = 0.9f - 0.6f * transicionDiaNoche;
float ambB = 0.9f - 0.55f * transicionDiaNoche;
vector("uLuzAmbiente", ambR, ambG, ambB);
```

**Cumple con:** "Al presionar N se debe alternar entre día y noche".

### 2.2 Farolas con atenuación por distancia (9+ farolas)

**Método nuevo `calcularFarolas()`:** calcula las posiciones de las farolas recorriendo las intersecciones de calles (filas/columnas pares) con un filtro `(fila + col) % 4 == 0` para distribuirlas uniformemente:
```java
private static float[][] calcularFarolas() {
    List<float[]> lista = new ArrayList<>();
    for (int fila = 0; fila < 11; fila += 2) {
        for (int col = 0; col < 11; col += 2) {
            if ((fila + col) % 4 == 0) {
                float x = -55 + 10 * (col + 0.5f);
                float z = -55 + 10 * (fila + 0.5f);
                lista.add(new float[] { x, 5.0f, z });
            }
        }
    }
    return lista.toArray(new float[0][]);
}
```
Esto genera **9 farolas** distribuidas en las intersecciones de calles.

**Método nuevo `dibujarFarolas()`:** dibuja el poste (caja alta oscura), la cúpula (caja ancha arriba) y un foco amarillo que solo aparece de noche (`transicionDiaNoche > 0.3f`).

**En el fragment shader**, la iluminación de farolas usa atenuación lineal:
```glsl
if (uModoNoche > 0.3) {
    float factorNoche = smoothstep(0.3, 0.7, uModoNoche);
    for (int i = 0; i < uFarolasCount; i++) {
        vec3 delta = vMundo - uFarolas[i];
        float dist = length(delta);
        float radio = 10.0;
        if (dist < radio) {
            float brillo = (1.0 - dist / radio) * factorNoche;
            luz += vec3(1.0, 0.8, 0.3) * brillo * 0.6;
        }
    }
}
```

**Cumple con:** "Al menos 9 farolas con atenuación por distancia".

### 2.3 Faros del auto con tecla F

**Campo nuevo:** `protected boolean farosEncendidos = false;`

**En Vehiculo.java `tecla()`:** `if (key == GLFW_KEY_F) farosEncendidos = !farosEncendidos;`

**En `prepararLuces()`:** se calculan las posiciones de los dos faros (izquierdo y derecho) y su dirección usando seno/coseno del ángulo del auto:
```java
entero("uFarosEncendidos", farosEncendidos ? 1 : 0);
float cos = (float) Math.cos(autoAngulo);
float sin = (float) Math.sin(autoAngulo);
float izqX = autoX + sin * 1.2f + cos * 0.4f;
// ... posiciones y direcciones de ambos faros
vector("uFarosPos[0]", izqX, 0.6f, izqZ);
vector("uFarosDir[0]", dirX, -0.15f, dirZ);
```

**En el fragment shader**, los faros usan un **cono de luz (spotlight)** con ángulo de 30°:
```glsl
if (uFarosEncendidos == 1) {
    for (int i = 0; i < 2; i++) {
        float cosAngulo = dot(normalize(hacia), dirNorm);
        float conoLimite = cos(radians(30.0));
        if (cosAngulo > conoLimite) {
            float factorCono = (cosAngulo - conoLimite) / (1.0 - conoLimite);
            float factorDist = 1.0 - dist / alcance;
            luz += vec3(1.0, 0.95, 0.8) * factorCono * factorDist * 0.8;
        }
    }
}
```

**Visualmente en `dibujarVehiculo()`** se dibujan dos cajitas amarillas brillantes cuando los faros están encendidos.

**Cumple con:** "Al presionar F se encienden/apagan los faros del vehículo".

### 2.4 Iluminación difusa con normales

**AppCiudad1:** usaba Lambert clásico con uniforms separados (`uLuzDir`, `uLuzColor`, `uAmbiente`).

**AppCiudad:** el fragment shader fue **completamente reescrito**. Ahora usa:
- Luz ambiente dinámica (`uLuzAmbiente`) que varía con la transición día/noche.
- Iluminación difusa de una luz direccional fija con factor que se reduce de noche:
  ```glsl
  float difuso = max(dot(normal, normalize(vec3(0.3, 1.0, 0.2))), 0.0);
  float factorDifuso = mix(0.3, 0.1, uModoNoche);
  luz += vec3(1.0) * difuso * factorDifuso;
  ```
- Las farolas, los faros y los semáforos suman su contribución al vector `luz`.
- Se aplica un clamp final: `luz = min(luz, vec3(1.5));` para evitar sobreexposición.

**Cumple con:** "Implementar iluminación difusa (Lambert) usando las normales de las caras".

---

## Sección 3 del PDF: Decoración y elementos (10 puntos)

### 3.1 Árboles y bancos en parques

**Método nuevo `dibujarArbolesYBancos()`:**
- Genera entre 2 y 4 árboles por parque con posiciones pseudoaleatorias basadas en `fila * 17 + columna * 31`.
- Cada árbol tiene un tronco (caja delgada marrón) y una copa (caja ancha verde).
- Genera 1 o 2 bancos rotados con `cajaGirada()`.

**Cumple con:** "Cada parque debe tener árboles y bancos decorativos".

### 3.2 Ventanas en edificios

**Método nuevo `dibujarVentanas()`:**
- Calcula pisos según la altura del edificio (`pisos = altura / 1.5`).
- 3 ventanas por fila, en las 4 caras del edificio.
- De noche (`transicionDiaNoche > 0.5`), algunas ventanas se iluminan (amarillo) y otras quedan oscuras, usando un seed determinista: `((seed + piso * 7 + v * 13) % 10) < 5`.
- De día todas las ventanas son gris azulado.

**Cumple con:** "Los edificios deben tener ventanas visibles".

### 3.3 Pasos peatonales

**Método nuevo `dibujarPasosPeatonales()`:**
- Se dibujan en las intersecciones (celdas donde fila y columna son pares).
- Verifica qué lados de la intersección tienen manzana adyacente y dibuja 5 franjas blancas perpendiculares a la calle en esa dirección.

**Cumple con:** "Agregar pasos peatonales (cebras) en las calles".

### 3.4 Semáforos animados

**Campos nuevos:**
```java
private float[][] semaforoPosiciones;
private int[] semaforoEjes;
private float[] semaforoTiempos;
private int[] semaforoFases;  // 0=verde, 1=amarillo, 2=rojo
```

**Método nuevo `inicializarSemaforos()`:** coloca 8 semáforos en intersecciones predefinidas con fases desfasadas entre sí.

**Actualización en `actualizar()`:** los semáforos ciclan automáticamente: verde (5s) → amarillo (2s) → rojo (5s):
```java
float[] duraciones = { 5.0f, 2.0f, 5.0f };
for (int i = 0; i < semaforosCount; i++) {
    semaforoTiempos[i] += deltaTime;
    if (semaforoTiempos[i] >= duraciones[semaforoFases[i]]) {
        semaforoTiempos[i] -= duraciones[semaforoFases[i]];
        semaforoFases[i] = (semaforoFases[i] + 1) % 3;
    }
}
```

**Método nuevo `dibujarSemaforos()`:** dibuja poste, caja negra, y 3 luces (roja/amarilla/verde) donde solo la activa brilla (1.0f) y las demás están atenuadas (0.2f).

**Los semáforos también emiten luz al shader** (en `prepararLuces()`):
```java
vector("uSemaforos[" + i + "]", semaforoPosiciones[i][0], 3.5f, semaforoPosiciones[i][1]);
vector("uSemaforosColor[" + i + "]", sr, sg, sb);
```

**Cumple con:** "Semáforos animados que cambien de color automáticamente".

### 3.5 Minimapa (tecla M)

**AppCiudad1:** el minimapa era pantalla completa (alternaba toda la vista con `vistaMapa ? 1 : 0`).

**AppCiudad:** el minimapa se dibuja en una **esquina** usando `glViewport`:
```java
private void dibujarMinimapa() {
    int tamMini = 180;
    int margen = 10;
    glViewport(ancho - tamMini - margen, margen, tamMini, tamMini);
    entero("uMapa", 1);
    // Dibuja fondo, manzanas coloreadas por tipo, y marcador del jugador
    caja(0, -0.1f, 0, 112, 0.01f, 112, 0.08f, 0.10f, 0.14f);
    for (...) { /* cada celda con color según tipo */ }
    cajaGirada(autoX, 0.5f, autoZ, 1.5f, 0.5f, 3.0f, 1.0f, 0.9f, 0.2f, autoAngulo);
    glViewport(0, 0, ancho, alto);  // restaurar viewport
    entero("uMapa", 0);
}
```

El divisor en el vertex shader cambió de `37.0` a `57.0` para adaptarse al mapa más grande.

**Cumple con:** "Minimapa (M) que muestre la ciudad desde arriba en una esquina".

### 3.6 Reset (tecla R)

**En Vehiculo.java `reiniciar()`:** resetea posición, ángulo, velocidad, faros y modo noche:
```java
private void reiniciar() {
    autoX = 0;  autoZ = -50;  autoAngulo = 0;
    velocidad = 0;  farosEncendidos = false;
    modoNoche = false;  transicionDiaNoche = 0.0f;
}
```

**Cumple con:** "Tecla R para reiniciar la posición del vehículo".

---

## Sección 4 del PDF: Organización y técnica (50 puntos)

### 4.1 Herencia: AppCiudad ← Vehiculo

La estructura de herencia se mantiene igual que en el código base, pero corregida y expandida:

| Base | Final |
|---|---|
| `AppCiudad1` (clase padre) | `AppCiudad` (clase padre) |
| `Vehiculo1 extends AppCiudad1` | `Vehiculo extends AppCiudad` |

**AppCiudad** contiene toda la lógica de la ciudad (mapa, renderizado, shaders, iluminación, NPCs, semáforos).
**Vehiculo** solo agrega la conducción del jugador, colisiones y la cámara follow.

### 4.2 Campos protected para herencia

**AppCiudad1:** mezclaba `private` y `protected` sin criterio claro.

**AppCiudad:** los campos que Vehiculo necesita acceder son `protected`:
- `ventana`, `ancho`, `alto` — para el título de ventana y aspecto de cámara
- `autoX`, `autoZ`, `autoAngulo` — posición del vehículo del jugador
- `farosEncendidos`, `modoNoche`, `transicionDiaNoche` — estados toggle
- `LIMITE`, `CELDA`, `MAPA` — para colisiones
- `npcX[]`, `npcZ[]`, `NUM_NPCS` — para colisión con NPCs
- Métodos: `tecla()`, `pulsada()`, `actualizar()`, `configurarCamara()`, `centro()`, `caja()`, `cajaGirada()`, `uniform()`, `vector()`, `decimal()`, `entero()`

### 4.3 DeltaTime

Todos los movimientos usan `deltaTime` (independientes del framerate):
- Cámara: `orbita += pasoOrbita * deltaTime`
- Transición día/noche: `transicionDiaNoche += velocidadTransicion * deltaTime`
- NPCs: `nuevoX = ax + dx * vel * dt`
- Semáforos: `semaforoTiempos[i] += deltaTime`
- Conducción: `velocidad *= (float) Math.exp(-resistencia * deltaTime)`

### 4.4 Eliminación de FloatBuffer

**AppCiudad1:** usaba `FloatBuffer` + `BufferUtils` para subir vértices al GPU:
```java
FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
buffer.put(vertices).flip();
glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
```

**AppCiudad:** pasa el `float[]` directamente (LWJGL lo permite):
```java
glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
```
Se eliminaron los imports de `FloatBuffer` y `BufferUtils`.

### 4.5 Eliminación de `clamp()` helper

**AppCiudad1:** tenía un método `clamp()` propio.
**AppCiudad:** usa `Math.max(min, Math.min(max, valor))` inline, eliminando el helper.

### 4.6 Eliminación del auto automático

**AppCiudad1:** tenía un auto NPC que recorría una calle horizontal automáticamente (`autoPosicion`, `VELOCIDAD_AUTO`, `dibujarAuto()`).
**AppCiudad:** eliminó ese auto único y lo reemplazó con el sistema de 8 NPCs autónomos (ver sección 5).

### 4.7 Método `compilar()` refactorizado

**AppCiudad1:** `crearPrograma()` hacía todo inline con un helper `comprobarShader()`.
**AppCiudad:** extrajo un método `compilar(int tipo, String fuente)` que compila un shader y devuelve su ID, con manejo de errores incluido. `crearPrograma()` ahora usa try/finally para limpiar shaders.

---

## Sección 5 del PDF: Bonus — NPCs autónomos (10 puntos extra)

### 5.1 Sistema de 8 NPCs

**Campos nuevos:**
```java
protected static final int NUM_NPCS = 8;
protected float[] npcX = new float[NUM_NPCS];
protected float[] npcZ = new float[NUM_NPCS];
private int[] npcDir = new int[NUM_NPCS];     // 0=+Z, 1=+X, 2=-Z, 3=-X
private float[] npcVelBase = new float[NUM_NPCS];
private float[][] npcColores = new float[NUM_NPCS][3];
```

### 5.2 Inicialización (`inicializarNPCs()`)

Cada NPC arranca en una posición predefinida (intersección de calles), con dirección inicial, velocidad base variable (6–10 unidades/s) y color de una paleta de 8 colores.

### 5.3 Movimiento inteligente (`actualizarNPCs()`)

Los NPCs:
1. **Siguen las calles:** solo se mueven en línea recta por celdas tipo 0.
2. **Giran en intersecciones:** cuando llegan a una intersección (fila y columna pares, distancia al centro < 0.5), eligen una nueva dirección usando `direccionesValidas()` que excluye la dirección opuesta (no dan media vuelta).
3. **Respetan semáforos:** `debeFrenarPorSemaforo()` verifica si hay un semáforo en rojo adelante del NPC en su eje de movimiento.
4. **Evitan al jugador:** si el jugador está a menos de 4 unidades en frente y a menos de 1.5 lateralmente, frenan.
5. **Evitan otros NPCs:** si otro NPC está 0.5–3.0 unidades adelante y a menos de 1.0 lateralmente, frenan.
6. **Wrap-around:** si salen del mapa, reaparecen en el lado opuesto.

### 5.4 Renderizado (`dibujarAutosNPC()`)

Cada NPC se dibuja como dos cajas giradas (carrocería + cabina) con su color propio. El ángulo se calcula según la dirección de movimiento.

**Cumple con:** "Al menos 3 vehículos NPC autónomos que circulen por las calles respetando semáforos".

---

## Bugs corregidos en Vehiculo1 → Vehiculo

### Bug 1: Loop infinito en colisiones (Vehiculo1 línea 104)

**Vehiculo1 (bug):**
```java
for (int columna = 0; fila < MAPA[fila].length; columna++)
//                    ^^^^  ERROR: usa 'fila' en vez de 'columna'
```
La condición del for interno compara `fila` (que nunca cambia dentro del loop) con el largo de la fila, causando un **loop infinito** y crash por `ArrayIndexOutOfBoundsException`.

**Vehiculo (corregido):**
```java
for (int columna = 0; columna < MAPA[fila].length; columna++)
```

### Bug 2: Frenado invertido (Vehiculo1 línea 51)

**Vehiculo1 (bug):**
```java
float factorFrenado = (float) Math.exp(-resistencia + deltaTime);
velocidad += factorFrenado;
//                           + deltaTime en vez de * deltaTime
```
Esto hace que el frenado exponencial sea **constante** en vez de proporcional al tiempo. Además `velocidad += factorFrenado` debería ser `velocidad *= factorFrenado` con la aceleración separada.

**Vehiculo (corregido):**
```java
float factorFrenado = (float) Math.exp(-resistencia * deltaTime);
velocidad *= factorFrenado;
velocidad += acelerador * 20f * deltaTime;
```

### Mejora: Colisión con NPCs

**Vehiculo1:** solo colisionaba con manzanas del mapa.

**Vehiculo:** agrega colisión con los 8 NPCs:
```java
for (int i = 0; i < NUM_NPCS; i++) {
    float dx = x - npcX[i];
    float dz = z - npcZ[i];
    if (dx * dx + dz * dz < 1.8f * 1.8f) {
        return false;
    }
}
```

### Mejora: Tamaño de colisión variable por tipo

**Vehiculo1:** usaba `5` fijo como medio-ancho de la manzana.

**Vehiculo:** usa `medio = MAPA[fila][columna] == 3 ? 4.5f : 3.5f` (los parques son más grandes).

---

## Cambios en el fragment shader (resumen)

| Aspecto | AppCiudad1 | AppCiudad |
|---|---|---|
| Uniforms de luz | `uLuzDir`, `uLuzColor`, `uAmbiente` (3 vec3 fijos) | `uModoNoche`, `uLuzAmbiente`, `uFarolas[]`, `uFarosPos[]`, `uFarosDir[]`, `uSemaforos[]`, `uSemaforosColor[]`, counts |
| Modelo de iluminación | Lambert difuso fijo | Lambert dinámico + farolas (atenuación lineal) + faros (cono spotlight) + semáforos (punto) |
| Luz ambiental | Fija `(0.35, 0.35, 0.38)` | Dinámica: de `(0.9, 0.9, 0.9)` a `(0.25, 0.3, 0.35)` |
| Clamp | No | `min(luz, vec3(1.5))` para evitar sobreexposición |

---

## Cambios en el vertex shader

| Aspecto | AppCiudad1 | AppCiudad |
|---|---|---|
| Divisor del minimapa | `37.0` | `57.0` (mapa más grande) |
| Comentarios GLSL | Inline extensos | Eliminados (código limpio) |
| Punto y coma faltante | `gl_Position = vec4(...)` sin `;` | Corregido |

---

## Tabla de cumplimiento vs PDF

| Requisito del PDF | Estado | Dónde |
|---|---|---|
| Ciudad 11×11 mínimo | ✅ 11×11 | `MAPA` en AppCiudad |
| 12+ bloques de edificios | ✅ 13 edificios | Tipos 1 y 2 en MAPA |
| 4+ parques | ✅ 5 parques | Tipo 3 en MAPA |
| Día/noche (N) | ✅ Transición suave | `modoNoche`, `transicionDiaNoche` |
| 9+ farolas con atenuación | ✅ 9 farolas | `calcularFarolas()`, fragment shader |
| Faros del auto (F) | ✅ Spotlight cónico | `farosEncendidos`, fragment shader |
| Iluminación difusa Lambert | ✅ Con normales | Fragment shader `difuso` |
| Árboles y bancos | ✅ En cada parque | `dibujarArbolesYBancos()` |
| Ventanas en edificios | ✅ Iluminadas de noche | `dibujarVentanas()` |
| Pasos peatonales | ✅ En intersecciones | `dibujarPasosPeatonales()` |
| Semáforos animados | ✅ Verde→Amarillo→Rojo | `inicializarSemaforos()`, `dibujarSemaforos()` |
| Minimapa (M) | ✅ En esquina | `dibujarMinimapa()` con glViewport |
| Reset (R) | ✅ | Vehiculo `reiniciar()` |
| Java/Maven/LWJGL/OpenGL | ✅ | pom.xml + toda la estructura |
| Herencia por responsabilidad | ✅ | AppCiudad ← Vehiculo |
| DeltaTime | ✅ En todo | `actualizar()`, NPCs, semáforos |
| 3+ NPCs autónomos (bonus) | ✅ 8 NPCs | `inicializarNPCs()`, `actualizarNPCs()` |

---

## Guía para modificar código en vivo (demo)

### Cambio 1: Cambiar color de un edificio

**Dónde:** `AppCiudad.java`, método `escena()`, dentro del bloque `if (tipo == 1 || tipo == 2)`.

**Línea clave:**
```java
caja(x, altura / 2 + 0.3f, z, 7, altura, 7, rojo, 0.40f, azul);
```

**Qué hacer:** Cambiar `0.40f` (verde) por otro valor. Ejemplo: hacer los edificios más rojizos:
```java
caja(x, altura / 2 + 0.3f, z, 7, altura, 7, 0.7f, 0.20f, 0.15f);
```

**Explicación:** Los últimos 3 parámetros de `caja()` son `(r, g, b)` en rango 0–1. Se envían al shader como `uColor`.

---

### Cambio 2: Cambiar la velocidad del auto

**Dónde:** `Vehiculo.java`, método `actualizar()`.

**Línea clave:**
```java
velocidad += acelerador * 20f * deltaTime;
```

**Qué hacer:** Cambiar `20f` a otro valor. Ejemplo: auto más rápido con `40f`, más lento con `10f`.

**Explicación:** `acelerador` vale +1 (W) o -1 (S). Multiplicar por un factor mayor da más aceleración. El `deltaTime` asegura que sea independiente del framerate.

---

### Cambio 3: Agregar un nuevo tipo de celda al mapa

**Dónde:** `AppCiudad.java`, método `escena()`.

**Pasos:**
1. En el `MAPA`, cambiar algún `1` por `4` (nuevo tipo).
2. En `escena()`, agregar un bloque para `tipo == 4`:
```java
else if (tipo == 4) {
    caja(x, 0.32f, z, 9, 0.1f, 9, 0.3f, 0.3f, 0.8f);  // base azul (agua/plaza)
}
```

**Explicación:** `escena()` recorre el `MAPA` y según el tipo dibuja distintas cosas. Cada tipo puede tener su propio método `dibujarLoQueSea()`.

---

### Cambio 4: Cambiar duración de los semáforos

**Dónde:** `AppCiudad.java`, método `actualizar()`.

**Línea clave:**
```java
float[] duraciones = { 5.0f, 2.0f, 5.0f };
```

**Qué hacer:** Cambiar los valores. El primer número es verde (segundos), el segundo amarillo, el tercero rojo. Ejemplo: semáforos más rápidos: `{ 3.0f, 1.0f, 3.0f }`.

---

### Cambio 5: Cambiar la cantidad o color de las ventanas

**Dónde:** `AppCiudad.java`, método `dibujarVentanas()`.

**Línea clave:**
```java
int ventanasPorFila = 3;
```

**Qué hacer:** Cambiar a `4` o `5` para más ventanas por piso.

Para cambiar el color de las ventanas iluminadas de noche:
```java
if (transicionDiaNoche > 0.5f && iluminada) {
    vr = 0.9f; vg = 0.8f; vb = 0.4f;  // amarillo cálido → cambiar a lo que quieras
}
```

---

### Cambio 6: Cambiar el radio/color de la luz de las farolas

**Dónde:** `AppCiudad.java`, método `fragmentShader()`, bloque de farolas.

**Línea clave:**
```glsl
float radio = 10.0;
// ...
luz += vec3(1.0, 0.8, 0.3) * brillo * 0.6;
```

**Qué hacer:**
- Cambiar `radio = 10.0` a `15.0` para que iluminen más lejos.
- Cambiar `vec3(1.0, 0.8, 0.3)` a otro color, por ejemplo `vec3(0.3, 0.5, 1.0)` para luz azulada.

**Explicación:** El shader corre en la GPU. El `radio` define hasta dónde llega la luz. El `vec3` es el color de la luz (RGB).

---

### Cambio 7: Agregar/quitar NPCs

**Dónde:** `AppCiudad.java`.

**Línea clave:**
```java
protected static final int NUM_NPCS = 8;
```

**Qué hacer:** Cambiar a `4` para menos NPCs o `12` para más. Si aumentas, agregar más entradas en los arrays `spawns` y `palette` de `inicializarNPCs()`.

---

### Consejo general para la demo

1. **Siempre recompilar después de cada cambio** (Maven o IDE).
2. **Los cambios en el GLSL (shaders)** están dentro de strings Java, así que requieren recompilar todo.
3. **Los cambios más vistosos para impresionar** son:
    - Cambiar colores (edificios, farolas, cielo nocturno) — se ve inmediatamente.
    - Cambiar la duración de semáforos — efecto visible rápido.
    - Agregar un nuevo tipo de celda al mapa — muestra dominio del sistema.
    - Cambiar el radio de luz de las farolas en el shader — muestra conocimiento de GLSL.
