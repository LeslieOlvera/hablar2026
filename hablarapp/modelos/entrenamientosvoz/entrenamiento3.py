# ============================================================
# MODELO CNN + LSTM PARA VALIDAR SI UNA PALABRA ESTÁ BIEN DICHA
# INTEGRACIÓN COMPLETA: FILTRADO, VAD, MULTI-AUGMENTATION Y ESCALA 0-100
# ============================================================

import os
import librosa
import numpy as np
import matplotlib.pyplot as plt
from scipy.signal import butter, lfilter

from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, confusion_matrix

import tensorflow as tf
from tensorflow.keras import layers, models, callbacks

# ============================================================
# 1. CONFIGURACIÓN GENERAL
# ============================================================

DATASET_PATH = "/content/drive/MyDrive/ejer18"
MODEL_OUTPUT_PATH = "/content/drive/MyDrive/modelo_validacion_ruso.keras"
AUDIO_PRUEBA = "/content/drive/MyDrive/ejer1/barril_bien/prueba3.ogg"

CLASES = {
    "ruso_mal": 0,
    "ruso_bien": 1
}

SR = 16000
DURATION = 2.0
N_SAMPLES = int(SR * DURATION)

N_MELS = 64
HOP_LENGTH = 256
N_FFT = 1024

# Umbral estricto solicitado
UMBRAL = 0.70

# ============================================================
# 2. PROCESAMIENTO AVANZADO: FILTRADO Y VAD
# ============================================================

def filtro_pasa_banda(audio, lowcut=80, highcut=7500, sr=SR, order=5):
    """Elimina ruidos fuera del espectro fundamental de la voz."""
    nyq = 0.5 * sr
    low = lowcut / nyq
    high = highcut / nyq
    b, a = butter(order, [low, high], btype='band')
    return lfilter(b, a, audio)

def aplicar_vad(audio, sr=SR, top_db=20):
    """Voice Activity Detection: Elimina silencios iniciales y finales."""
    intervalos = librosa.effects.split(audio, top_db=top_db)
    if len(intervalos) > 0:
        audio_voz = np.concatenate([audio[start:end] for start, end in intervalos])
        return audio_voz
    return audio

# ============================================================
# 3. FUNCIONES DE AUMENTO DE DATOS (DATA AUGMENTATION)
# ============================================================

def agregar_ruido(audio, factor_ruido=0.005):
    """Inyecta ruido blanco aleatorio."""
    ruido = np.random.normal(0, factor_ruido, len(audio))
    return audio + ruido

def cambiar_velocidad(audio):
    """Cambia la velocidad sin alterar el tono."""
    factor = np.random.uniform(0.85, 1.15)
    return librosa.effects.time_stretch(y=audio, rate=factor)

def cambiar_tono(audio, sr=SR):
    """Modifica el tono (pitch) sin alterar la velocidad."""
    pasos = np.random.uniform(-2.0, 2.0)
    return librosa.effects.pitch_shift(y=audio, sr=sr, n_steps=pasos)

def aplicar_spec_augment(mel_db, num_f_masks=1, num_t_masks=1, f_width=8, t_width=12):
    """SpecAugment: Bloquea bandas de frecuencia y tiempo en el espectrograma."""
    mel_aug = mel_db.copy()
    num_frecuencias, num_frames = mel_aug.shape

    # Máscaras de Frecuencia (Líneas horizontales)
    for _ in range(num_f_masks):
        f = np.random.randint(0, num_frecuencias - f_width)
        mel_aug[f:f + f_width, :] = np.mean(mel_aug)

    # Máscaras de Tiempo (Líneas verticales)
    for _ in range(num_t_masks):
        t = np.random.randint(0, num_frames - t_width)
        mel_aug[:, t:t + t_width] = np.mean(mel_aug)

    return mel_aug

# ============================================================
# 4. FUNCIÓN MAESTRA DE CARGA Y NORMALIZACIÓN
# ============================================================

def cargar_y_procesar_audio(ruta_audio, sr=SR, n_samples=N_SAMPLES, tipo_aug=None):
    """Carga, filtra, aplica VAD y una técnica de aumento específica o ninguna."""
    try:
        audio, _ = librosa.load(ruta_audio, sr=sr, mono=True)
    except Exception as e:
        raise IOError(f"Error al leer {ruta_audio}: {e}")

    # Procesamiento base idéntico para todos
    audio = filtro_pasa_banda(audio, sr=sr)
    audio = aplicar_vad(audio, sr=sr)

    # Aplicar la técnica correspondiente según el bucle de entrenamiento
    if tipo_aug == 'ruido':
        audio = agregar_ruido(audio)
    elif tipo_aug == 'velocidad':
        audio = cambiar_velocidad(audio)
    elif tipo_aug == 'tono':
        audio = cambiar_tono(audio, sr=sr)

    # Normalizar amplitud máxima
    if np.max(np.abs(audio)) > 0:
        audio = audio / np.max(np.abs(audio))

    # Ajustar a tamaño fijo
    if len(audio) > n_samples:
        audio = audio[:n_samples]
    else:
        audio = np.pad(audio, (0, n_samples - len(audio)), mode="constant")

    return audio

def audio_a_mel(audio):
    """Transforma el audio limpio en un Mel-Espectrograma."""
    mel = librosa.feature.melspectrogram(
        y=audio, sr=SR, n_fft=N_FFT, hop_length=HOP_LENGTH, n_mels=N_MELS
    )
    mel_db = librosa.power_to_db(mel, ref=np.max)
    mel_db = (mel_db - np.mean(mel_db)) / (np.std(mel_db) + 1e-8)
    return mel_db

# ============================================================
# 5. CARGA DEL DATASET CON AUMENTO MASIVO MULTI-TÉCNICA
# ============================================================

X = []
y = []
extensiones_validas = [".wav", ".mp3", ".m4a", ".ogg", ".flac"]

print("Cargando, filtrando y multiplicando el dataset mediante Data Augmentation...")

for nombre_clase, etiqueta in CLASES.items():
    carpeta = os.path.join(DATASET_PATH, nombre_clase)
    if not os.path.exists(carpeta):
        print(f"Advertencia: No existe la carpeta {carpeta}")
        continue

    archivos = [f for f in os.listdir(carpeta) if any(f.lower().endswith(ext) for ext in extensiones_validas)]

    for archivo in archivos:
        ruta = os.path.join(carpeta, archivo)
        try:
            # 1. Muestra Original Limpia
            audio_orig = cargar_y_procesar_audio(ruta, tipo_aug=None)
            mel_orig = audio_a_mel(audio_orig)
            X.append(mel_orig)
            y.append(etiqueta)

            # 2. Variante con Ruido Blanco
            audio_ruido = cargar_y_procesar_audio(ruta, tipo_aug='ruido')
            mel_ruido = audio_a_mel(audio_ruido)
            X.append(mel_ruido)
            y.append(etiqueta)

            # 3. Variante con Alteración de Velocidad
            audio_vel = cargar_y_procesar_audio(ruta, tipo_aug='velocidad')
            mel_vel = audio_a_mel(audio_vel)
            X.append(mel_vel)
            y.append(etiqueta)

            # 4. Variante con Alteración de Tono (Pitch)
            audio_tono = cargar_y_procesar_audio(ruta, tipo_aug='tono')
            mel_tono = audio_a_mel(audio_tono)
            X.append(mel_tono)
            y.append(etiqueta)

            # 5. Variante con SpecAugment (basada en el audio original)
            mel_spec_aug = aplicar_spec_augment(mel_orig)
            X.append(mel_spec_aug)
            y.append(etiqueta)

        except Exception as e:
            print(f"Error procesando {archivo}: {e}")

X = np.array(X)
y = np.array(y)

print(f"\n¡Dataset Expandido con éxito!: {X.shape[0]} muestras en total.")
print(f"Clase MAL (0): {np.sum(y == 0)} | Clase BIEN (1): {np.sum(y == 1)}")

if len(X) == 0:
    raise ValueError("Cero audios procesados. Verifica tus rutas en Drive.")

X = X[..., np.newaxis]

# ============================================================
# 6. DIVISIÓN DEL DATASET
# ============================================================

X_train, X_temp, y_train, y_temp = train_test_split(
    X, y, test_size=0.20, random_state=42, stratify=y
)
X_val, X_test, y_val, y_test = train_test_split(
    X_temp, y_temp, test_size=0.50, random_state=42, stratify=y_temp
)

# ============================================================
# 7. MODELO REDISEÑADO PARA EVITAR OVERFITTING
# ============================================================

input_shape = X_train.shape[1:]

modelo = models.Sequential([
    layers.Input(shape=input_shape),

    layers.Conv2D(32, (3, 3), padding="same", activation="relu"),
    layers.BatchNormalization(),
    layers.MaxPooling2D((2, 2)),
    layers.Dropout(0.3),

    layers.Conv2D(64, (3, 3), padding="same", activation="relu"),
    layers.BatchNormalization(),
    layers.MaxPooling2D((2, 2)),
    layers.Dropout(0.3),

    layers.Reshape((-1, 64)),

    layers.Bidirectional(layers.LSTM(32, return_sequences=False)),
    layers.Dropout(0.4),

    layers.Dense(32, activation="relu"),
    layers.Dropout(0.4),

    layers.Dense(1, activation="sigmoid")
])

modelo.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=0.0003),
    loss="binary_crossentropy",
    metrics=["accuracy"]
)

# ============================================================
# 8. CALLBACKS ESTRATÉGICOS
# ============================================================

early_stop = callbacks.EarlyStopping(
    monitor="val_loss", patience=15, restore_best_weights=True
)
reduce_lr = callbacks.ReduceLROnPlateau(
    monitor="val_loss", factor=0.5, patience=6, min_lr=1e-6
)
checkpoint = callbacks.ModelCheckpoint(
    MODEL_OUTPUT_PATH, monitor="val_loss", save_best_only=True
)

# ============================================================
# 9. ENTRENAMIENTO
# ============================================================

print("\nIniciando entrenamiento intensivo con dataset aumentado...")
historial = modelo.fit(
    X_train, y_train,
    validation_data=(X_val, y_val),
    epochs=120,
    batch_size=16,
    callbacks=[early_stop, reduce_lr, checkpoint],
    verbose=1
)

# ============================================================
# 10. EVALUACIÓN DE MÉTRICAS CON EL UMBRAL DE 0.70
# ============================================================

y_prob = modelo.predict(X_test).flatten()
y_pred = (y_prob >= UMBRAL).astype(int)

print(f"\n=== REPORTE DE CLASIFICACIÓN (UMBRAL: {UMBRAL}) ===")
print(classification_report(y_test, y_pred, target_names=["MAL", "BIEN"], zero_division=0))

# ============================================================
# 11. FUNCIÓN DE PREDICCIÓN CON ESCALA DE CALIFICACIÓN (0 A 100)
# ============================================================

def predecir_calificacion_voz(ruta_audio, modelo_red, umbral=UMBRAL):
    """
    Evalúa un audio devolviendo una nota de 0 a 100 basada en el umbral.
    [0 - 50): Mal dicho.
    [50 - 100]: Bien dicho.
    """
    if not os.path.exists(ruta_audio):
        print(f"El archivo de prueba no existe en la ruta: {ruta_audio}")
        return

    audio = cargar_y_procesar_audio(ruta_audio, tipo_aug=None)
    mel = audio_a_mel(audio)
    X_audio = mel[np.newaxis, ..., np.newaxis]

    prob_bien = modelo_red.predict(X_audio, verbose=0)[0][0]

    if prob_bien < umbral:
        calificacion = (prob_bien / umbral) * 50
    else:
        calificacion = 50 + ((prob_bien - umbral) / (1.0 - umbral)) * 50

    print("\n====================================")
    print("      RESULTADO DE AUDIO NUEVO      ")
    print("====================================")
    print(f"Ruta: {ruta_audio}")
    print(f"Score obtenido: {calificacion:.2f} / 100")
    print(f"Probabilidad cruda: {prob_bien:.4f} (Umbral corte: {umbral})")

    if calificacion >= 50:
        print("Dictamen: BIEN DICHO 🎉")
    else:
        print("Dictamen: MAL DICHO ❌")

    return calificacion

# ============================================================
# 12. EJECUCIÓN DE PRUEBA INDIVIDUAL
# ============================================================

if os.path.exists(AUDIO_PRUEBA):
    predecir_calificacion_voz(AUDIO_PRUEBA, modelo, UMBRAL)
else:
    print(f"\nConfigura un audio real en 'AUDIO_PRUEBA' para testear la escala 0-100.")
