package com.graphics;

import static org.lwjgl.glfw.GLFW.*;

public class Vehiculo extends AppCiudad {
    private float velocidad = 0;
    private boolean cameraArea = false;
    private boolean chataVisible = false;
    private float chataX, chataZ;
    private float chataAngulo;
    private static final float ENGANCHE_DIST = 2.0f;
    private static final float CHATA_LARGO = 2.2f;
    private static final float RADIO_CHATA = 1.4f;
    private static final float MAX_ANGULO_CHATA = 0.6f;
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

        if (key == GLFW_KEY_E) {
            chataVisible = !chataVisible;
            if (chataVisible) {
                inicializarChata();
            }
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
        chataVisible = false;
        inicializarChata();
    }

    private void inicializarChata() {
        float sinA = (float) Math.sin(autoAngulo);
        float cosA = (float) Math.cos(autoAngulo);
        chataX = autoX - sinA * (ENGANCHE_DIST + CHATA_LARGO * 0.5f);
        chataZ = autoZ - cosA * (ENGANCHE_DIST + CHATA_LARGO * 0.5f);
        chataAngulo = autoAngulo;
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

        if (chataVisible) {
            actualizarChata();
        }

        actualizarTitulo();
    }

    private void actualizarChata() {
        float sinA = (float) Math.sin(autoAngulo);
        float cosA = (float) Math.cos(autoAngulo);
        float enganX = autoX - sinA * ENGANCHE_DIST;
        float enganZ = autoZ - cosA * ENGANCHE_DIST;

        float dx = enganX - chataX;
        float dz = enganZ - chataZ;
        float dist = (float) Math.sqrt(dx * dx + dz * dz);

        if (dist > 0.01f) {
            float anguloDeseado = (float) Math.atan2(dx, dz);

            float diferencia = anguloDeseado - autoAngulo;
            while (diferencia > Math.PI) diferencia -= 2 * Math.PI;
            while (diferencia < -Math.PI) diferencia += 2 * Math.PI;
            diferencia = Math.max(-MAX_ANGULO_CHATA, Math.min(MAX_ANGULO_CHATA, diferencia));

            chataAngulo = autoAngulo + diferencia;

            float sinC = (float) Math.sin(chataAngulo);
            float cosC = (float) Math.cos(chataAngulo);
            float nuevaX = enganX - sinC * CHATA_LARGO * 0.5f;
            float nuevaZ = enganZ - cosC * CHATA_LARGO * 0.5f;

            if (chataColisiona(nuevaX, nuevaZ)) {
                velocidad *= 0.3f;
                return;
            }

            chataX = nuevaX;
            chataZ = nuevaZ;
        }
    }

    private boolean chataColisiona(float x, float z) {
        float limitePermitido = LIMITE - RADIO_CHATA;
        if (Math.abs(x) > limitePermitido || Math.abs(z) > limitePermitido) {
            return true;
        }

        float sinC = (float) Math.sin(chataAngulo);
        float cosC = (float) Math.cos(chataAngulo);
        float[] offsets = {-CHATA_LARGO * 0.4f, 0, CHATA_LARGO * 0.4f};

        for (float off : offsets) {
            float px = x + sinC * off;
            float pz = z + cosC * off;

            for (int fila = 0; fila < MAPA.length; fila++) {
                for (int columna = 0; columna < MAPA[fila].length; columna++) {
                    if (MAPA[fila][columna] == 0) continue;
                    float centroX = centro(columna);
                    float centroZ = centro(fila);
                    float medio = MAPA[fila][columna] == 3 ? 4.5f : 3.5f;
                    float cercaX = Math.max(centroX - medio, Math.min(px, centroX + medio));
                    float cercaZ = Math.max(centroZ - medio, Math.min(pz, centroZ + medio));
                    float distX = px - cercaX;
                    float distZ = pz - cercaZ;
                    if (distX * distX + distZ * distZ < RADIO_CHATA * RADIO_CHATA) {
                        return true;
                    }
                }
            }

            for (int i = 0; i < NUM_NPCS; i++) {
                float ndx = px - npcX[i];
                float ndz = pz - npcZ[i];
                if (ndx * ndx + ndz * ndz < 1.8f * 1.8f) {
                    return true;
                }
            }
        }

        return false;
    }

    private void actualizarTitulo() {
        int kilometrosPorHora = Math.round(Math.abs(velocidad) * 3.6f);
        String titulo = getClass().getSimpleName();

        titulo += " | WASD/flechas: conducir | Espacio: freno ";
        titulo += "| F: faros | N: noche | M: minimapa | E: chata ";
        titulo += "| C: camara | R: reiniciar ";
        titulo += kilometrosPorHora + " km/h";
        titulo += estadoExtra();
        glfwSetWindowTitle(ventana, titulo);
    }

    protected String estadoExtra() {
        return chataVisible ? " | [CHATA]" : "";
    }

    @Override
    protected void dibujarVehiculo() {
        super.dibujarVehiculo();

        if (chataVisible) {
            float cosC = (float) Math.cos(chataAngulo);
            float sinC = (float) Math.sin(chataAngulo);

            cajaGirada(chataX, 0.35f, chataZ, 1.6f, 0.08f, CHATA_LARGO, 0.25f, 0.25f, 0.28f, chataAngulo);

            float barandaY = 0.55f;
            float barandaH = 0.25f;
            float mitadAncho = 0.75f;

            float izqX = chataX + cosC * mitadAncho;
            float izqZ = chataZ - sinC * mitadAncho;
            cajaGirada(izqX, barandaY, izqZ, 0.06f, barandaH, CHATA_LARGO, 0.20f, 0.20f, 0.22f, chataAngulo);

            float derX = chataX - cosC * mitadAncho;
            float derZ = chataZ + sinC * mitadAncho;
            cajaGirada(derX, barandaY, derZ, 0.06f, barandaH, CHATA_LARGO, 0.20f, 0.20f, 0.22f, chataAngulo);

            float trasOffsetZ = -CHATA_LARGO * 0.5f;
            float trasX = chataX + sinC * trasOffsetZ;
            float trasZ = chataZ + cosC * trasOffsetZ;
            cajaGirada(trasX, barandaY, trasZ, 1.6f, barandaH, 0.06f, 0.20f, 0.20f, 0.22f, chataAngulo);

            float[][] ruedas = {{0.75f, -0.7f}, {-0.75f, -0.7f}};
            for (float[] r : ruedas) {
                float rx = chataX + cosC * r[0] + sinC * r[1];
                float rz = chataZ - sinC * r[0] + cosC * r[1];
                cajaGirada(rx, 0.18f, rz, 0.15f, 0.35f, 0.4f, 0.1f, 0.1f, 0.1f, chataAngulo);
            }

            float barraFrenteX = chataX + sinC * (CHATA_LARGO * 0.5f);
            float barraFrenteZ = chataZ + cosC * (CHATA_LARGO * 0.5f);
            float sinA = (float) Math.sin(autoAngulo);
            float cosA = (float) Math.cos(autoAngulo);
            float enganX = autoX - sinA * ENGANCHE_DIST;
            float enganZ = autoZ - cosA * ENGANCHE_DIST;
            float barraX = (barraFrenteX + enganX) * 0.5f;
            float barraZ = (barraFrenteZ + enganZ) * 0.5f;
            float bdx = enganX - barraFrenteX;
            float bdz = enganZ - barraFrenteZ;
            float barraLen = (float) Math.sqrt(bdx * bdx + bdz * bdz);
            float barraAng = (float) Math.atan2(bdx, bdz);
            if (barraLen > 0.05f) {
                cajaGirada(barraX, 0.35f, barraZ, 0.08f, 0.08f, barraLen, 0.30f, 0.30f, 0.30f, barraAng);
            }
        }
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
