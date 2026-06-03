# ============================================================
# MODELO CNN + LSTM PARA VALIDAR SI UNA PALABRA ESTÁ BIEN DICHA
# PLAN 1 INCLUIDO: UMBRAL ESTRICTO = 0.70
# Clasificación binaria: MAL vs BIEN
# ============================================================

import os
import librosa
import numpy as np
import matplotlib.pyplot as plt

from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, confusion_matrix

import tensorflow as tf
from tensorflow.keras import layers, models, callbacks


# ============================================================
# 1. CONFIGURACIÓN GENERAL
# ============================================================

DATASET_PATH = "/content/drive/MyDrive/ejer1"

MODEL_OUTPUT_PATH = "/content/drive/MyDrive/modelo_validacion_voz.keras"

AUDIO_PRUEBA = "/content/drive/MyDrive/ejer1/barril_bien/prueba3.ogg"

CLASES = {
    "barril_mal": 0,
    "barril_bien": 1
}

SR = 16000
DURATION = 2.0
N_SAMPLES = int(SR * DURATION)

N_MELS = 64
HOP_LENGTH = 256
N_FFT = 1024

UMBRAL = 0.70


# ============================================================
# 2. FUNCIÓN PARA CARGAR Y NORMALIZAR AUDIO
# ============================================================

def cargar_audio(ruta_audio, sr=SR, n_samples=N_SAMPLES):
    """
    Carga un audio, lo convierte a mono, lo re-muestrea a 16 kHz
    y lo ajusta a una duración fija.
    """

    audio, _ = librosa.load(ruta_audio, sr=sr, mono=True)

    # Normalizar amplitud
    if np.max(np.abs(audio)) > 0:
        audio = audio / np.max(np.abs(audio))

    # Recortar si dura más de la duración fija
    if len(audio) > n_samples:
        audio = audio[:n_samples]

    # Rellenar con ceros si dura menos
    if len(audio) < n_samples:
        audio = np.pad(audio, (0, n_samples - len(audio)), mode="constant")

    return audio


# ============================================================
# 3. FUNCIÓN PARA CONVERTIR AUDIO A MEL-ESPECTROGRAMA
# ============================================================

def audio_a_mel(audio):
    """
    Convierte el audio en mel-espectrograma normalizado.
    """

    mel = librosa.feature.melspectrogram(
        y=audio,
        sr=SR,
        n_fft=N_FFT,
        hop_length=HOP_LENGTH,
        n_mels=N_MELS
    )

    mel_db = librosa.power_to_db(mel, ref=np.max)

    # Normalización z-score
    mel_db = (mel_db - np.mean(mel_db)) / (np.std(mel_db) + 1e-8)

    return mel_db


# ============================================================
# 4. CARGA DEL DATASET
# ============================================================

X = []
y = []

extensiones_validas = [".wav", ".mp3", ".m4a", ".ogg", ".flac"]

print("Cargando audios...")

for nombre_clase, etiqueta in CLASES.items():
    carpeta = os.path.join(DATASET_PATH, nombre_clase)

    if not os.path.exists(carpeta):
        print(f"No existe la carpeta: {carpeta}")
        continue

    archivos = os.listdir(carpeta)

    for archivo in archivos:
        if not any(archivo.lower().endswith(ext) for ext in extensiones_validas):
            continue

        ruta = os.path.join(carpeta, archivo)

        try:
            audio = cargar_audio(ruta)
            mel = audio_a_mel(audio)

            X.append(mel)
            y.append(etiqueta)

        except Exception as e:
            print(f"Error con archivo {ruta}: {e}")

X = np.array(X)
y = np.array(y)

print("\nDataset cargado.")
print("Forma inicial de X:", X.shape)
print("Forma inicial de y:", y.shape)
print("Total clase MAL:", np.sum(y == 0))
print("Total clase BIEN:", np.sum(y == 1))


# ============================================================
# 5. VALIDACIONES BÁSICAS DEL DATASET
# ============================================================

if len(X) == 0:
    raise ValueError("No se cargaron audios. Revisa la ruta del dataset.")

if len(np.unique(y)) < 2:
    raise ValueError("Necesitas audios en ambas carpetas: bien y mal.")

# Agregar canal para CNN
X = X[..., np.newaxis]

print("Forma final de X:", X.shape)


# ============================================================
# 6. DIVISIÓN TRAIN / VALIDATION / TEST
# ============================================================

X_train, X_temp, y_train, y_temp = train_test_split(
    X,
    y,
    test_size=0.30,
    random_state=42,
    stratify=y
)

X_val, X_test, y_val, y_test = train_test_split(
    X_temp,
    y_temp,
    test_size=0.50,
    random_state=42,
    stratify=y_temp
)

print("\nDivisión del dataset:")
print("Train:", X_train.shape)
print("Validation:", X_val.shape)
print("Test:", X_test.shape)


# ============================================================
# 7. DEFINICIÓN DEL MODELO CNN + LSTM
# ============================================================

input_shape = X_train.shape[1:]

modelo = models.Sequential([
    layers.Input(shape=input_shape),

    layers.Conv2D(32, (3, 3), padding="same", activation="relu"),
    layers.BatchNormalization(),
    layers.MaxPooling2D((2, 2)),
    layers.Dropout(0.25),

    layers.Conv2D(64, (3, 3), padding="same", activation="relu"),
    layers.BatchNormalization(),
    layers.MaxPooling2D((2, 2)),
    layers.Dropout(0.25),

    layers.Conv2D(128, (3, 3), padding="same", activation="relu"),
    layers.BatchNormalization(),
    layers.MaxPooling2D((2, 1)),
    layers.Dropout(0.30),

    # Convertir salida CNN a secuencia para LSTM
    layers.Reshape((-1, 128)),

    layers.Bidirectional(layers.LSTM(64, return_sequences=False)),
    layers.Dropout(0.40),

    layers.Dense(64, activation="relu"),
    layers.Dropout(0.30),

    layers.Dense(1, activation="sigmoid")
])

modelo.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=0.0005),
    loss="binary_crossentropy",
    metrics=[
        "accuracy",
        tf.keras.metrics.Precision(name="precision"),
        tf.keras.metrics.Recall(name="recall")
    ]
)

modelo.summary()


# ============================================================
# 8. CALLBACKS
# ============================================================

early_stop = callbacks.EarlyStopping(
    monitor="val_loss",
    patience=10,
    restore_best_weights=True
)

reduce_lr = callbacks.ReduceLROnPlateau(
    monitor="val_loss",
    factor=0.5,
    patience=5,
    min_lr=1e-6
)

checkpoint = callbacks.ModelCheckpoint(
    MODEL_OUTPUT_PATH,
    monitor="val_loss",
    save_best_only=True
)


# ============================================================
# 9. ENTRENAMIENTO
# ============================================================

historial = modelo.fit(
    X_train,
    y_train,
    validation_data=(X_val, y_val),
    epochs=80,
    batch_size=16,
    callbacks=[early_stop, reduce_lr, checkpoint]
)


# ============================================================
# 10. EVALUACIÓN NORMAL DEL MODELO
# ============================================================

print("\nEvaluación directa del modelo en test:")
resultados = modelo.evaluate(X_test, y_test, verbose=1)

for nombre, valor in zip(modelo.metrics_names, resultados):
    print(f"{nombre}: {valor:.4f}")


# ============================================================
# 11. EVALUACIÓN CON UMBRAL ESTRICTO 0.70
# ============================================================

y_prob = modelo.predict(X_test).flatten()

y_pred = (y_prob >= UMBRAL).astype(int)

print("\n====================================")
print(f"EVALUACIÓN CON UMBRAL = {UMBRAL}")
print("====================================")

print("\nMatriz de confusión:")
print(confusion_matrix(y_test, y_pred))

print("\nReporte de clasificación:")
print(classification_report(
    y_test,
    y_pred,
    target_names=["MAL", "BIEN"]
))


# ============================================================
# 12. PROBAR VARIOS UMBRALES
# ============================================================

umbrales = [0.50, 0.55, 0.60, 0.65, 0.70, 0.75, 0.80]

print("\n====================================")
print("COMPARACIÓN DE DIFERENTES UMBRALES")
print("====================================")

for u in umbrales:
    y_pred_u = (y_prob >= u).astype(int)

    print("\n------------------------------")
    print(f"UMBRAL: {u}")
    print("------------------------------")

    print("Matriz de confusión:")
    print(confusion_matrix(y_test, y_pred_u))

    print("Reporte:")
    print(classification_report(
        y_test,
        y_pred_u,
        target_names=["MAL", "BIEN"],
        zero_division=0
    ))


# ============================================================
# 13. GRÁFICAS DE ENTRENAMIENTO
# ============================================================

plt.figure(figsize=(8, 5))
plt.plot(historial.history["accuracy"], label="Accuracy entrenamiento")
plt.plot(historial.history["val_accuracy"], label="Accuracy validación")
plt.xlabel("Época")
plt.ylabel("Accuracy")
plt.title("Accuracy del modelo")
plt.legend()
plt.grid()
plt.show()

plt.figure(figsize=(8, 5))
plt.plot(historial.history["loss"], label="Loss entrenamiento")
plt.plot(historial.history["val_loss"], label="Loss validación")
plt.xlabel("Época")
plt.ylabel("Loss")
plt.title("Pérdida del modelo")
plt.legend()
plt.grid()
plt.show()


# ============================================================
# 14. GUARDAR MODELO FINAL
# ============================================================

modelo.save(MODEL_OUTPUT_PATH)

print(f"\nModelo guardado en: {MODEL_OUTPUT_PATH}")


# ============================================================
# 15. FUNCIÓN PARA PROBAR UN AUDIO NUEVO
# ============================================================

def predecir_audio_nuevo(ruta_audio, modelo, umbral=UMBRAL):
    """
    Predice si un audio nuevo está BIEN o MAL dicho.
    """

    audio = cargar_audio(ruta_audio)
    mel = audio_a_mel(audio)

    X_audio = np.array(mel)
    X_audio = X_audio[np.newaxis, ..., np.newaxis]

    prob_bien = modelo.predict(X_audio)[0][0]

    print("\n====================================")
    print("PREDICCIÓN DE AUDIO NUEVO")
    print("====================================")
    print(f"Audio: {ruta_audio}")
    print(f"Probabilidad de estar BIEN dicho: {prob_bien * 100:.2f}%")
    print(f"Umbral usado: {umbral}")

    if prob_bien >= umbral:
        print("Resultado: BIEN DICHO")
    else:
        print("Resultado: MAL DICHO")

    return prob_bien


# ============================================================
# 16. PROBAR AUDIO INDIVIDUAL
# ============================================================

if os.path.exists(AUDIO_PRUEBA):
    predecir_audio_nuevo(AUDIO_PRUEBA, modelo, UMBRAL)
else:
    print("\nNo se encontró AUDIO_PRUEBA.")
    print("Cambia la ruta en AUDIO_PRUEBA si quieres probar un audio nuevo.")
