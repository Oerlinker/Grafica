# Tasks

## Task 1: Expandir grid del mapa de 7x7 a 11x11
- Archivos: AppCiudad.java
- Descripcion: Ampliar la matriz `MAPA[][]` de 7x7 a 11x11. Cambiar `LIMITE` de 35 a 55 manteniendo `CELDA = 10`. Poblar la nueva cuadricula con calles (0), edificios altos (1), edificios bajos (2) y parques (3) distribuidos de forma variada. Ajustar el suelo a 110x110 unidades y la distancia de camara proporcionalmente.
- Criterio: Mapa 11x11 con 121 celdas renderizadas, al menos 12 manzanas con edificios y 4+ parques. Formula `centro()` calcula posiciones correctas para indices 0-10.
- Dependencias: ninguna
- Spec: mapa-ampliado/spec.md

## Task 2: Crear clase Vehiculo con camara en tercera persona
- Archivos: Vehiculo.java (nuevo), AppCiudad.java
- Descripcion: Extraer el vehiculo a una clase `Vehiculo` con estado propio: posicion (x, z), angulo, velocidad y booleano de faros. Implementar controles W/S para adelante/atras y A/D para girar. Reemplazar la camara orbital por una camara en tercera persona que sigue al vehiculo desde atras y arriba. Dibujar el vehiculo como conjunto de cajas (cuerpo, techo, ruedas).
- Criterio: El vehiculo se mueve por la ciudad con WASD, la camara lo sigue suavemente, el vehiculo es visible como geometria 3D.
- Dependencias: Task 1 (necesita el mapa expandido para recorrer)

## Task 3: Reescribir fragment shader con iluminacion
- Archivos: AppCiudad.java (metodo fragmentShader())
- Descripcion: Reemplazar el fragment shader minimal (`color = uColor`) por uno que calcule iluminacion per-fragment. Agregar uniforms: `uModoNoche` (float 0-1), `uLuzAmbiente` (vec3), `uFarolas[20]` (vec3[]), `uFarolasCount` (int), `uFarosPos[2]` (vec3[]), `uFarosDir[2]` (vec3[]), `uFarosEncendidos` (int). Implementar modelo: color final = uColor * (ambiente + sum(contribuciones_luces)). Pasar `vMundo` y `vNormal` desde el vertex shader al fragment shader.
- Criterio: La escena se ilumina correctamente con luz ambiental. Los uniforms de luces existen y compilan aunque aun no se envien datos reales. El vertex shader pasa vMundo y vNormal al fragment shader sin errores.
- Dependencias: ninguna (puede hacerse en paralelo con Task 1)

## Task 4: Sistema de iluminacion dia/noche
- Archivos: AppCiudad.java
- Descripcion: Agregar estado `modoNoche` (boolean) y `transicionDiaNoche` (float 0.0-1.0) que interpola suavemente entre dia y noche en ~4 segundos. En modo dia: glClearColor azul claro (0.5, 0.7, 0.9), uLuzAmbiente alta. En modo noche: glClearColor oscuro (0.05, 0.1, 0.2), uLuzAmbiente baja (20-30%). Toggle con tecla N (o automatico). Enviar `uModoNoche` al shader cada frame.
- Criterio: Al pulsar N la escena transiciona suavemente entre dia y noche en ~4 segundos. Los colores de fondo y la iluminacion ambiental cambian gradualmente. No hay parpadeos ni saltos bruscos.
- Dependencias: Task 3 (necesita el shader con soporte de uModoNoche y uLuzAmbiente)
- Spec: iluminacion-dia-noche/spec.md

## Task 5: Farolas en intersecciones
- Archivos: AppCiudad.java
- Descripcion: Definir 15-20 posiciones de farolas en intersecciones del mapa 11x11. Dibujar cada farola como poste gris oscuro (caja delgada, 6u alto) con luminaria en la parte superior. Enviar posiciones al shader via `uFarolas[]` y `uFarolasCount`. En el fragment shader, calcular iluminacion circular ambar (1.0, 0.8, 0.3) con radio ~8u y atenuacion radial (brightness = max(0, 1 - d/R)). Las farolas solo emiten luz cuando `uModoNoche > 0.5`.
- Criterio: 15-20 farolas visibles como postes en intersecciones. En modo noche proyectan circulos de luz ambar en el suelo con brillo decreciente. En modo dia no emiten luz. El shader procesa el array completo sin errores.
- Dependencias: Task 3 (shader con arrays de luces), Task 4 (modo noche controla activacion)
- Spec: farolas/spec.md

## Task 6: Faros del vehiculo
- Archivos: Vehiculo.java, AppCiudad.java
- Descripcion: Agregar dos focos delanteros al vehiculo, separados simetricamente ~0.4u a cada lado. Calcular posicion y direccion de cada foco a partir de la posicion/angulo del vehiculo cada frame. Enviar `uFarosPos[2]`, `uFarosDir[2]`, `uFarosEncendidos` al shader. En el fragment shader, implementar spotlight conico: alcance 15-20u, angulo 30 grados, color blanco calido (1.0, 0.95, 0.8), atenuacion por distancia y por angulo. Toggle con tecla F.
- Criterio: Al pulsar F se encienden/apagan los faros. Los conos de luz siguen la orientacion del vehiculo al girar. La luz ilumina el suelo y edificios dentro del alcance. Los faros funcionan tanto de dia como de noche.
- Dependencias: Task 2 (vehiculo con posicion y angulo), Task 3 (shader con uniforms de faros)
- Spec: faros-vehiculo/spec.md

## Task 7: Decoracion urbana — arboles y bancos en parques
- Archivos: AppCiudad.java
- Descripcion: En cada celda tipo parque, dibujar 2-4 arboles (tronco marron cilindrico + copa verde esferica/conica, altura 2-4u) y 1-2 bancos (prisma rectangular color madera, ~1.5x0.5x0.5u) con posiciones variadas dentro de la celda. Usar variacion leve en tamano y orientacion (0/90/180/270 grados para bancos).
- Criterio: Cada parque tiene arboles con tronco y copa distinguibles y bancos de aspecto madera. Las posiciones varian entre parques para evitar uniformidad. No hay solapamiento entre decoracion.
- Dependencias: Task 1 (necesita celdas tipo parque en el mapa 11x11)
- Spec: decoracion-urbana/spec.md (REQ-DECO-001, REQ-DECO-002)

## Task 8: Decoracion urbana — ventanas en edificios
- Archivos: AppCiudad.java
- Descripcion: Dibujar ventanas como cajas planas (profundidad minima ~0.05u) sobre las caras de cada edificio, en patron de cuadricula (2-3 filas, 3-4 columnas por cara). En modo dia: color azul-gris (0.3, 0.35, 0.45). En modo noche: 40-60% iluminadas en amarillo calido (0.9, 0.8, 0.4), resto oscuras (0.05, 0.05, 0.1). El patron de iluminacion se determina al inicio con seed basada en posicion y permanece constante.
- Criterio: Los edificios tienen ventanas visibles en patron regular. De noche, algunas ventanas brillan en amarillo. El patron es consistente frame a frame. No hay z-fighting entre ventanas y cara del edificio.
- Dependencias: Task 1 (edificios en el mapa), Task 4 (modoNoche para alternar colores)
- Spec: decoracion-urbana/spec.md (REQ-DECO-003, REQ-DECO-004)

## Task 9: Decoracion urbana — pasos peatonales
- Archivos: AppCiudad.java
- Descripcion: Dibujar pasos peatonales (cebras) en celdas tipo calle adyacentes a edificios o parques. Cada paso peatonal consiste en 4-6 franjas blancas (~0.3u ancho) alternadas con el asfalto, cruzando la calle (~2u de largo). Posicionar a Y = 0.01 para evitar z-fighting con el suelo.
- Criterio: Los pasos peatonales son visibles como franjas blancas en calles junto a manzanas. No hay z-fighting ni parpadeo. Los pasos estan correctamente orientados segun la direccion de la calle.
- Dependencias: Task 1 (necesita el mapa expandido para detectar adjacencia)
- Spec: decoracion-urbana/spec.md (REQ-DECO-005)

## Task 10: Semaforos con secuencia temporal
- Archivos: AppCiudad.java
- Descripcion: Implementar 6-10 semaforos en intersecciones principales. Cada semaforo tiene: poste gris oscuro, caja con tres luces apiladas (rojo/amarillo/verde). Estado temporal con fases: verde (5s) -> amarillo (2s) -> rojo (5s), ciclico. Solo la luz activa tiene brillo completo; las inactivas al 20%. Semaforos perpendiculares desfasados medio ciclo (6s). Offset inicial aleatorio por interseccion (0-12s). En modo noche, la luz activa emite resplandor sutil (radio 3-5u, 50% intensidad de farola).
- Criterio: Los semaforos cambian de color correctamente en la secuencia verde->amarillo->rojo. Los semaforos perpendiculares nunca estan ambos en verde. En modo noche emiten resplandor del color activo. La geometria (poste + caja + luces) es visible.
- Dependencias: Task 1 (intersecciones del mapa), Task 3 (shader para resplandor nocturno), Task 4 (modoNoche)
- Spec: semaforos/spec.md

## Task 11: Minimapa con viewport separado
- Archivos: AppCiudad.java
- Descripcion: Despues de la escena principal, configurar `glViewport(ancho - 190, 10, 180, 180)` y activar `uMapa = 1` para proyeccion ortografica. Dibujar la cuadricula 11x11 como rectangulos planos coloreados por tipo de celda (gris = edificio, verde = parque, oscuro = calle). Dibujar el vehiculo como triangulo amarillo rotado segun su angulo. Dibujar destino activo como punto rojo si existe. Norte siempre arriba. Toggle con tecla M. Recalcular posicion del viewport al redimensionar la ventana.
- Criterio: El minimapa muestra toda la ciudad en vista top-down en la esquina inferior derecha. El vehiculo aparece como triangulo orientado correctamente. Tecla M muestra/oculta el minimapa. Al redimensionar, el minimapa se mantiene en la esquina. El minimapa no dibuja decoracion (solo celdas planas + vehiculo).
- Dependencias: Task 1 (mapa 11x11), Task 2 (posicion del vehiculo)
- Spec: minimapa/spec.md

## Task 12: Reinicio del vehiculo y estado
- Archivos: Vehiculo.java, AppCiudad.java
- Descripcion: Implementar tecla R para reiniciar el vehiculo a su posicion y angulo inicial. Opcionalmente reiniciar el modo dia/noche a dia y los faros a apagados.
- Criterio: Al pulsar R el vehiculo vuelve a la posicion inicial instantaneamente. La camara se reposiciona suavemente. Los faros se apagan.
- Dependencias: Task 2 (vehiculo), Task 4 (modo dia/noche), Task 6 (faros)

## Orden de implementacion sugerido

```
Task 1 (grid)  ──────────────────┐
Task 3 (shader) ─────────┐       │
                          ▼       ▼
                     Task 4 (dia/noche)
                          │       │
              ┌───────────┤       │
              ▼           ▼       ▼
         Task 5 (farolas)  Task 2 (vehiculo)
                                  │
                          ┌───────┼───────┐
                          ▼       ▼       ▼
                    Task 6    Task 11   Task 7
                    (faros)   (minimapa) (arboles)
                                        Task 8 (ventanas)
                                        Task 9 (peatonales)
                          Task 10 (semaforos)
                          Task 12 (reinicio) ← ultimo
```

Fases recomendadas:
1. **Base**: Tasks 1 + 3 (en paralelo) — grid expandido y shader con iluminacion
2. **Iluminacion**: Task 4 — ciclo dia/noche funcional
3. **Elementos principales**: Tasks 2 + 5 (en paralelo) — vehiculo y farolas
4. **Features del vehiculo**: Tasks 6 + 11 (en paralelo) — faros y minimapa
5. **Decoracion**: Tasks 7 + 8 + 9 + 10 (en paralelo) — arboles, ventanas, cebras, semaforos
6. **Cierre**: Task 12 — reinicio
