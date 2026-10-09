# Especificacion: Decoracion Urbana

## What (Que debe hacer el sistema)

El sistema debe agregar elementos decorativos urbanos a la ciudad: arboles y bancos en parques, ventanas iluminadas en edificios durante la noche, y pasos peatonales (cebras) en las calles junto a las manzanas.

## Requirements (Requerimientos)

### REQ-DECO-001: Arboles en Parques
**Prioridad**: Alta  
**Estado**: Pendiente

El sistema debe colocar arboles en las celdas tipo parque:
- 2-4 arboles por celda de parque, distribuidos aleatoriamente dentro de la celda
- Cada arbol consiste en un tronco cilindrico marron (0.35f, 0.25f, 0.1f) y copa esferica o conica verde (0.15f, 0.5f, 0.1f)
- Altura del arbol: 2-4 unidades
- Radio de la copa: 1-1.5 unidades
- Variacion leve en tamano y posicion para evitar uniformidad artificial

### REQ-DECO-002: Bancos en Parques
**Prioridad**: Media  
**Estado**: Pendiente

El sistema debe colocar bancos de parque en celdas tipo parque:
- 1-2 bancos por celda de parque
- Representados como prismas rectangulares simples (largo ~1.5u, alto ~0.5u, profundidad ~0.5u)
- Color marron madera (0.45f, 0.30f, 0.15f)
- Posicionados junto a senderos o bordes del parque
- Orientacion aleatoria (0, 90, 180 o 270 grados)

### REQ-DECO-003: Ventanas en Edificios
**Prioridad**: Alta  
**Estado**: Pendiente

El sistema debe dibujar ventanas en las caras de los edificios:
- Patron de cuadricula regular en cada cara visible del edificio
- Cada ventana: rectangulo pequeno (aprox. 0.4 x 0.6 unidades)
- Espaciado uniforme entre ventanas (1-1.5 unidades entre centros)
- En modo dia: ventanas de color azul oscuro/gris (0.3f, 0.35f, 0.45f) simulando reflejo

### REQ-DECO-004: Ventanas Iluminadas de Noche
**Prioridad**: Alta  
**Estado**: Pendiente

En modo noche, una proporcion de ventanas debe aparecer iluminadas:
- 40-60% de las ventanas se iluminan con color amarillo calido (0.9f, 0.8f, 0.4f)
- El patron de ventanas iluminadas se determina al inicio y permanece constante durante la sesion
- Las ventanas no iluminadas se ven oscuras (0.05f, 0.05f, 0.1f)
- El efecto es puramente visual (no emiten luz real al entorno)

### REQ-DECO-005: Pasos Peatonales (Cebras)
**Prioridad**: Alta  
**Estado**: Pendiente

El sistema debe dibujar pasos peatonales en las calles adyacentes a manzanas:
- Franjas blancas alternadas con el color del asfalto
- Ancho del paso peatonal: ~2 unidades (cruzando la calle)
- 4-6 franjas blancas por paso peatonal, cada una de ~0.3u de ancho
- Posicionados en los bordes de las celdas de calle donde colindan con edificios o parques
- Dibujados ligeramente sobre el suelo (Y = 0.01) para evitar z-fighting

## Scenarios (Escenarios de Uso)

### SCENARIO-DECO-001: Renderizado de Arboles en Parque
**Activacion**: Al dibujar una celda tipo parque

```
Dado que una celda del mapa es de tipo parque
Cuando se ejecuta el metodo de dibujo para esa celda
Entonces se dibujan entre 2 y 4 arboles con posiciones variadas dentro de la celda
Y cada arbol tiene un tronco marron y una copa verde
Y la altura y tamano varian ligeramente entre arboles
```

### SCENARIO-DECO-002: Renderizado de Bancos en Parque
**Activacion**: Al dibujar una celda tipo parque

```
Dado que una celda del mapa es de tipo parque
Cuando se ejecuta el metodo de dibujo para esa celda
Entonces se dibujan 1-2 bancos como prismas rectangulares color madera
Y los bancos estan orientados en angulos de 0, 90, 180 o 270 grados
Y estan posicionados cerca de los bordes de la celda
```

### SCENARIO-DECO-003: Ventanas de Edificio en Modo Dia
**Activacion**: Al dibujar un edificio con modoNoche = false

```
Dado que se esta dibujando un edificio de altura H
Y el sistema esta en modo dia
Cuando se renderizan las caras del edificio
Entonces se dibujan ventanas en patron de cuadricula con color azul-gris (0.3, 0.35, 0.45)
Y las ventanas estan espaciadas uniformemente en filas y columnas
```

### SCENARIO-DECO-004: Ventanas Iluminadas en Modo Noche
**Activacion**: Al dibujar un edificio con modoNoche = true

```
Dado que se esta dibujando un edificio de altura H
Y el sistema esta en modo noche
Cuando se renderizan las caras del edificio
Entonces el 40-60% de las ventanas se dibujan con color amarillo calido (0.9, 0.8, 0.4)
Y el resto de ventanas se dibujan oscuras (0.05, 0.05, 0.1)
Y el patron de iluminacion es consistente frame a frame
```

### SCENARIO-DECO-005: Pasos Peatonales en Calles
**Activacion**: Al dibujar celdas tipo calle adyacentes a edificios o parques

```
Dado que una celda es de tipo calle
Y tiene al menos una celda adyacente de tipo edificio o parque
Cuando se dibuja la calle
Entonces se renderizan franjas blancas alternadas en el borde adyacente a la manzana
Y las franjas estan a Y = 0.01 para evitar z-fighting con el suelo
Y el paso peatonal tiene 4-6 franjas de ~0.3 unidades de ancho
```

### SCENARIO-DECO-006: Transicion Dia/Noche de Ventanas
**Activacion**: Al cambiar entre modo dia y modo noche

```
Dado que las ventanas de los edificios estan visibles
Cuando el sistema transiciona de modo dia a modo noche
Entonces las ventanas cambian de color azul-gris a su estado nocturno (iluminadas o oscuras)
Y la transicion sigue el mismo ritmo que la transicion general dia/noche
```

## Acceptance Criteria (Criterios de Aceptacion)

- [ ] Los parques deben tener entre 2-4 arboles con tronco y copa visibles
- [ ] Los parques deben tener 1-2 bancos de apariencia madera
- [ ] Los edificios deben tener ventanas en patron de cuadricula en sus caras
- [ ] En modo noche, 40-60% de las ventanas deben estar iluminadas en amarillo calido
- [ ] Los pasos peatonales deben estar en calles adyacentes a manzanas con franjas blancas
- [ ] Los pasos peatonales no deben tener z-fighting con el suelo
- [ ] La decoracion no debe degradar significativamente el rendimiento

## Context (Contexto)

La decoracion urbana se implementa como geometria adicional dibujada dentro de los metodos existentes de dibujo de celdas. Los arboles y bancos se agregan al metodo de dibujo de parques, las ventanas al de edificios, y los pasos peatonales al de calles. Las ventanas iluminadas requieren conocer el estado modoNoche para alternar colores.
