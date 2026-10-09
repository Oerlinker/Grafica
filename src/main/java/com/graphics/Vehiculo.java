package com.graphics;

import static org.lwjgl.glfw.GLFW.*;

public class Vehiculo extends AppCiudad {
    private float velocidad = 0;
    private boolean cameraArea = false;
    private static final float RADIO_AUTO = 1.65f;

    @Override
    protected void tecla(int key) {
        super.tecla(key);

        if (key == GLFW_KEY_F) {
            farosEncendidos = !farosEncendidos;
        }

        if (key == GLFW_KEY_C) {
            cameraArea = !cameraArea;
        }

        if (key == GLFW_KEY_R) {
            reiniciar();
        }
    }

    private void reiniciar() {
        autoX = 0;
        autoZ = -50;
        autoAngulo = 0;
        velocidad = 0;
        farosEncendidos = false;
        modoNoche = false;
        transicionDiaNoche = 0.0f;
    }

    // ==================== Conduccion ====================
    @Override
    protected void actualizar(float deltaTime) {
        super.actualizar(deltaTime);

        float acelerador = 0;

        if (pulsada(GLFW_KEY_W) || pulsada(GLFW_KEY_UP)) {
            acelerador += 1;
        }

        if (pulsada(GLFW_KEY_S) || pulsada(GLFW_KEY_DOWN)) {
            acelerador -= 1;
        }

        float resistencia = 0.7f;
        if (pulsada(GLFW_KEY_SPACE)) {
            resistencia = 7;
        }

        float factorFrenado = (float) Math.exp(-resistencia * deltaTime);
        velocidad *= factorFrenado;
        velocidad += acelerador * 20f * deltaTime;
        velocidad = Math.max(-6, Math.min(16, velocidad));

        float direccion = 0;

        if (pulsada(GLFW_KEY_A) || pulsada(GLFW_KEY_LEFT)) {
            direccion += 1;
        }

        if (pulsada(GLFW_KEY_D) || pulsada(GLFW_KEY_RIGHT)) {
            direccion -= 1;
        }

        autoAngulo += direccion * velocidad * 0.11f * deltaTime;
        float frenteX = (float) Math.sin(autoAngulo);
        float frenteZ = (float) Math.cos(autoAngulo);
        float siguienteX = autoX + frenteX * velocidad * deltaTime;
        float siguienteZ = autoZ + frenteZ * velocidad * deltaTime;

        if (puedeCircular(siguienteX, siguienteZ)) {
            autoX = siguienteX;
            autoZ = siguienteZ;
        } else {
            velocidad = 0;
        }

        actualizarTitulo();
    }

    private void actualizarTitulo() {
        int kilometrosPorHora = Math.round(Math.abs(velocidad) * 3.6f);
        String titulo = getClass().getSimpleName();

        titulo += " | WASD/flechas: conducir | Espacio: freno ";
        titulo += "| F: faros | N: noche | M: minimapa ";
        titulo += "| C: camara | R: reiniciar ";
        titulo += kilometrosPorHora + " km/h";
        titulo += estadoExtra();
        glfwSetWindowTitle(ventana, titulo);
    }

    protected String estadoExtra() {
        return "";
    }

    // ==================== Colisiones ====================
    private boolean puedeCircular(float x, float z) {
        float limitePermitido = LIMITE - RADIO_AUTO;

        if (Math.abs(x) > limitePermitido || Math.abs(z) > limitePermitido) {
            return false;
        }

        for (int fila = 0; fila < MAPA.length; fila++) {
            for (int columna = 0; columna < MAPA[fila].length; columna++) {
                if (MAPA[fila][columna] == 0) {
                    continue;
                }
                float centroX = centro(columna);
                float centroZ = centro(fila);
                float medio = MAPA[fila][columna] == 3 ? 4.5f : 3.5f;
                float cercaX = Math.max(centroX - medio, Math.min(x, centroX + medio));
                float cercaZ = Math.max(centroZ - medio, Math.min(z, centroZ + medio));
                float distanciaX = x - cercaX;
                float distanciaZ = z - cercaZ;
                float distanciaCuadrada = distanciaX * distanciaX + distanciaZ * distanciaZ;

                if (distanciaCuadrada < RADIO_AUTO * RADIO_AUTO) {
                    return false;
                }
            }
        }

        for (int i = 0; i < NUM_NPCS; i++) {
            float dx = x - npcX[i];
            float dz = z - npcZ[i];
            if (dx * dx + dz * dz < 1.8f * 1.8f) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void configurarCamara() {
        if (cameraArea) {
            super.configurarCamara();
            return;
        }

        float camaraX = autoX - (float) Math.sin(autoAngulo) * 12;
        float camaraZ = autoZ - (float) Math.cos(autoAngulo) * 12;
        vector("uOjo", camaraX, 9, camaraZ);
        vector("uObjetivo", autoX, 0.8f, autoZ);
        decimal("uAspecto", (float) ancho / alto);
    }

    public static void main(String[] args) {
        new Vehiculo().run();
    }
}
