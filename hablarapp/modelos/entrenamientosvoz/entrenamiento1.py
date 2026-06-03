# ============================================================
# SCRIPT 1: ENTRENAMIENTO AUTOMÁTICO DE 18 MODELOS INDEPENDIENTES
# ============================================================

import os
import librosa
import numpy as np
from scipy.signal import butter, lfilter
from sklearn.model_selection import train_test_split
import tensorflow as tf
from tensorflow.keras import layers, models, callbacks

# 1. CONFIGURACIÓN RAÍZ
DRIVE_PATH = "/content/drive/MyDrive"
SR = 16000
DURATION = 2.0
N_SAMPLES = int(SR * DURATION)
N_MELS = 64
HOP_LENGTH = 256
N_FFT = 1024

# Estructura correlativa de los 18 ejercicios mapeando a su palabra clave
EJERCICIOS = {
    1: "barril", 2: "cigarro", 3: "ferrocarril", 4: "ra",
    5: "rama", 6: "re", 7: "ri", 8: "rita",
    9: "ro", 10: "rojo", 11: "rosa", 12: "rra",
    13: "rre", 14: "rri", 15: "rro", 16: "rru",
    17: "ru", 18: "ruso"
}

# ============================================================
# 2. PROCESAMIENTO, VAD Y DATA AUGMENTATION
# ============================================================

def filtro_pasa_banda(audio, lowcut=80, highcut=7500, sr=SR, order=5):
    nyq = 0.5 * sr
    low = lowcut / nyq
    high = highcut / nyq
    b, a = butter(order, [low, high], btype='band')
    return lfilter(b, a, audio)

def aplicar_vad(audio, sr=SR, top_db=20):
    intervalos = librosa.effects.split(audio, top_db=top_db)
    if len(intervalos) > 0:
        return np.concatenate([audio[start:end] for start, end in intervalos])
    return audio

def agregar_ruido(audio, factor_ruido=0.005):
    return audio + np.random.normal(0, factor_ruido, len(audio))

def cambiar_velocidad(audio):
    factor = np.random.uniform(0.85, 1.15)
    return librosa.effects.time_stretch(y=audio, rate=factor)

def cambiar_tono(audio, sr=SR):
    pasos = np.random.uniform(-2.0, 2.0)
    return librosa.effects.pitch_shift(y=audio, sr=sr, n_steps=pasos)

def aplicar_spec_augment(mel_db, num_f_masks=1, num_t_masks=1, f_width=8, t_width=12):
    mel_aug = mel_db.copy()
    num_frecuencias, num_frames = mel_aug.shape
    for _ in range(num_f_masks):
        f = np.random.randint(0, num_frecuencias - f_width)
        mel_aug[f:f + f_width, :] = np.mean(mel_aug)
    for _ in range(num_t_masks):
        t = np.random.randint(0, num_frames - t_width)
        mel_aug[:, t:t + t_width] = np.mean(mel_aug)
    return mel_aug

def cargar_y_procesar_audio(ruta_audio, sr=SR, n_samples=N_SAMPLES, tipo_aug=None):
    try:
        audio, _ = librosa.load(ruta_audio, sr=sr, mono=True)
    except Exception as e:
        raise IOError(f"Error al leer {ruta_audio}: {e}")

    audio = filtro_pasa_banda(audio, sr=sr)
    audio = aplicar_vad(audio, sr=sr)

    if tipo_aug == 'ruido':
        audio = agregar_ruido(audio)
    elif tipo_aug == 'velocidad':
        audio = cambiar_velocidad(audio)
    elif tipo_aug == 'tono':
        audio = cambiar_tono(audio, sr=sr)

    if np.max(np.abs(audio)) > 0:
        audio = audio / np.max(np.abs(audio))

    if len(audio) > n_samples:
        audio = audio[:n_samples]
    else:
        audio = np.pad(audio, (0, n_samples - len(audio)), mode="constant")
    return audio

def audio_a_mel(audio):
    mel = librosa.feature.melspectrogram(y=audio, sr=SR, n_fft=N_FFT, hop_length=HOP_LENGTH, n_mels=N_MELS)
    mel_db = librosa.power_to_db(mel, ref=np.max)
    return (mel_db - np.mean(mel_db)) / (np.std(mel_db) + 1e-8)

# ============================================================
# 3. CONSTRUCTOR DEL MODELO NEURONAL (Limpio para cada ciclo)
# ============================================================

def construir_modelo(input_shape):
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
    return modelo

# ============================================================
# 4. BUCLE MAESTRO DE ENTRENAMIENTO (Ejer1 a Ejer18)
# ============================================================

extensiones_validas = [".wav", ".mp3", ".m4a", ".ogg", ".flac"]

for num_ejer, palabra in EJERCICIOS.items():
    print("\n" + "="*70)
    print(f"INICIANDO PROCESAMIENTO: EJER {num_ejer} - PALABRA: {palabra.upper()}")
    print("="*70)

    ejer_folder_path = os.path.join(DRIVE_PATH, f"ejer{num_ejer}")
    if not os.path.exists(ejer_folder_path):
        print(f"Error crítico: No existe el directorio {ejer_folder_path}. Pasando al siguiente.")
        continue

    clases_locales = {
        f"{palabra}_mal": 0,
        f"{palabra}_bien": 1
    }

    X = []
    y = []

    for folder_name, etiqueta in clases_locales.items():
        carpeta_clase = os.path.join(ejer_folder_path, folder_name)
        if not os.path.exists(carpeta_clase):
            print(f"  Aviso: Faltaba la ruta {carpeta_clase}, buscando sin prefijo...")
            # Fallback por si la carpeta se llama solo 'bien' o 'mal'
            alternativa = "bien" if "bien" in folder_name else "mal"
            carpeta_clase = os.path.join(ejer_folder_path, alternativa)
            if not os.path.exists(carpeta_clase):
                print(f"  Error: No se encontró carpeta para la clase {folder_name}")
                continue

        archivos = [f for f in os.listdir(carpeta_clase) if any(f.lower().endswith(ext) for ext in extensiones_validas)]
        print(f"  Procesando {len(archivos)} archivos originales en: {folder_name}")

        for archivo in archivos:
            ruta_audio = os.path.join(carpeta_clase, archivo)
            try:
                # Datos base y aumentos integrados (Multiplica x5 el dataset de forma nativa)
                audio_orig = cargar_y_procesar_audio(ruta_audio, tipo_aug=None)
                mel_orig = audio_a_mel(audio_orig)
                X.append(mel_orig)
                y.append(etiqueta)

                X.append(audio_a_mel(cargar_y_procesar_audio(ruta_audio, tipo_aug='ruido')))
                y.append(etiqueta)

                X.append(audio_a_mel(cargar_y_procesar_audio(ruta_audio, tipo_aug='velocidad')))
                y.append(etiqueta)

                X.append(audio_a_mel(cargar_y_procesar_audio(ruta_audio, tipo_aug='tono')))
                y.append(etiqueta)

                X.append(aplicar_spec_augment(mel_orig))
                y.append(etiqueta)
            except Exception as e:
                pass

    X = np.array(X)
    y = np.array(y)

    if len(X) == 0:
        print(f"No se pudieron recolectar datos para {palabra}. Cancelando entrenamiento de este ejercicio.")
        continue

    X = X[..., np.newaxis]

    # Separación estratificada de datos
    X_train, X_temp, y_train, y_temp = train_test_split(X, y, test_size=0.20, random_state=42, stratify=y)
    X_val, X_test, y_val, y_test = train_test_split(X_temp, y_temp, test_size=0.50, random_state=42, stratify=y_temp)

    # Reset de la sesión de Keras y construcción
    tf.keras.backend.clear_session()
    model_instance = construir_modelo(X_train.shape[1:])

    model_save_name = os.path.join(DRIVE_PATH, f"{palabra}modelo.keras")

    early_stop = callbacks.EarlyStopping(monitor="val_loss", patience=15, restore_best_weights=True)
    reduce_lr = callbacks.ReduceLROnPlateau(monitor="val_loss", factor=0.5, patience=6, min_lr=1e-6)
    checkpoint = callbacks.ModelCheckpoint(model_save_name, monitor="val_loss", save_best_only=True, verbose=0)

    print(f"  Entrenando modelo con {X_train.shape[0]} muestras aumentadas...")
    model_instance.fit(
        X_train, y_train,
        validation_data=(X_val, y_val),
        epochs=120,
        batch_size=16,
        callbacks=[early_stop, reduce_lr, checkpoint],
        verbose=0 # Mantener consola limpia para el bucle de 18 modelos
    )

    # Forzar el guardado de la versión optimizada final
    model_instance.save(model_save_name)
    print(f"  ✔️ Éxito total. Modelo exportado a: {model_save_name}")

print("\n PROCESO FINALIZADO: Los 18 modelos han sido creados y resguardados en tu Drive.")
