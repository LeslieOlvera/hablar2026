(base) ubuntu@ip-172-31-45-54:~/apps/hablar2026/hablarapp/backend/src/ml$ cat clasificar_orofacial.py
import os
import sys
import cv2
import json
import tempfile
import numpy as np
from PIL import Image, ImageOps
import tensorflow as tf
from tensorflow import keras


BASE_DIR = os.path.dirname(os.path.abspath(__file__))

MODEL_PATH = os.path.join(BASE_DIR, "modelo_orofaciales.keras")
CLASS_NAMES_PATH = os.path.join(BASE_DIR, "class_names.json")


def preprocess_single_image(input_path, output_path, size=(224, 224)):
    img = Image.open(input_path)
    img = ImageOps.exif_transpose(img)
    img = img.convert("RGB")
    img = img.resize(size, Image.Resampling.LANCZOS)

    img_np = np.array(img)
    gray = cv2.cvtColor(img_np, cv2.COLOR_RGB2GRAY)

    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
    final_img = clahe.apply(gray)

    cv2.imwrite(output_path, final_img)
    return output_path


def interpretar_resultado(pred_class, confianza):
    if pred_class.lower().endswith("mal"):
        porcentaje_mostrado = 100.0 - confianza
        estado = "mal"
    else:
        porcentaje_mostrado = confianza
        estado = "bien"

    return porcentaje_mostrado, estado


def cargar_modelo(model_path):
    """
    Carga el modelo en dos posibles formatos:
    1. Archivo .keras o .h5
    2. Carpeta SavedModel
    """

    if os.path.isfile(model_path):
        modelo = keras.models.load_model(model_path)
        return {
            "tipo": "keras_file",
            "modelo": modelo
        }

    if os.path.isdir(model_path):
        modelo = tf.saved_model.load(model_path)

        if "serving_default" not in modelo.signatures:
            raise ValueError(
                "El modelo es una carpeta, pero no tiene firma 'serving_default'."
            )

        infer = modelo.signatures["serving_default"]

        return {
            "tipo": "saved_model",
            "modelo": infer
        }

    raise FileNotFoundError(f"No existe el modelo: {model_path}")


def predecir_con_modelo(modelo_info, img_array):
    tipo = modelo_info["tipo"]
    modelo = modelo_info["modelo"]

    if tipo == "keras_file":
        pred = modelo.predict(img_array, verbose=0)[0]
        return pred

    if tipo == "saved_model":
        tensor_input = tf.constant(img_array)

        salida = modelo(tensor_input)

        if isinstance(salida, dict):
            primer_valor = list(salida.values())[0]
            pred = primer_valor.numpy()[0]
            return pred

        pred = salida.numpy()[0]
        return pred

    raise ValueError(f"Tipo de modelo no soportado: {tipo}")


def predecir_imagen_preprocesada(image_path, modelo_info, class_names, img_size=(224, 224)):
    img = cv2.imread(image_path, cv2.IMREAD_GRAYSCALE)

    if img is None:
        raise ValueError(f"No se pudo leer la imagen preprocesada: {image_path}")

    img = cv2.resize(img, img_size)
    img_rgb = cv2.cvtColor(img, cv2.COLOR_GRAY2RGB)

    img_array = img_rgb.astype(np.float32)
    img_array = np.expand_dims(img_array, axis=0)

    pred = predecir_con_modelo(modelo_info, img_array)

    pred_idx = int(np.argmax(pred))
    pred_class = class_names[pred_idx]
    confianza_real = float(pred[pred_idx]) * 100.0

    porcentaje_mostrado, estado = interpretar_resultado(
        pred_class,
        confianza_real
    )

    probabilidades = []

    for clase, prob in zip(class_names, pred):
        probabilidades.append({
            "clase": clase,
            "probabilidad": round(float(prob) * 100.0, 2)
        })

    probabilidades = sorted(
        probabilidades,
        key=lambda x: x["probabilidad"],
        reverse=True
    )

    return {
        "clase": pred_class,
        "confianza_real": round(confianza_real, 2),
        "porcentaje": round(porcentaje_mostrado, 2),
        "estado": estado,
        "probabilidades": probabilidades
    }


def main():
    temp_output_path = None

    try:
        if len(sys.argv) < 2:
            raise ValueError("Falta la ruta de la imagen como argumento.")

        input_image_path = sys.argv[1]

        if not os.path.exists(input_image_path):
            raise FileNotFoundError(f"No existe la imagen: {input_image_path}")

        if not os.path.exists(MODEL_PATH):
            raise FileNotFoundError(f"No existe el modelo: {MODEL_PATH}")

        if not os.path.exists(CLASS_NAMES_PATH):
            raise FileNotFoundError(f"No existe class_names.json: {CLASS_NAMES_PATH}")

        with open(CLASS_NAMES_PATH, "r", encoding="utf-8") as f:
            class_names = json.load(f)

        modelo_info = cargar_modelo(MODEL_PATH)

        with tempfile.NamedTemporaryFile(
            suffix=".png",
            prefix="preprocesada_",
            delete=False
        ) as temp_file:
            temp_output_path = temp_file.name

        imagen_preprocesada_path = preprocess_single_image(
            input_path=input_image_path,
            output_path=temp_output_path
        )

        resultado = predecir_imagen_preprocesada(
            image_path=imagen_preprocesada_path,
            modelo_info=modelo_info,
            class_names=class_names,
            img_size=(224, 224)
        )

        respuesta = {
            "ok": True,
            "archivo_entrada": input_image_path,
            "clase": resultado["clase"],
            "confianza_real": resultado["confianza_real"],
            "porcentaje": resultado["porcentaje"],
            "estado": resultado["estado"],
            "probabilidades": resultado["probabilidades"]
        }

        print(json.dumps(respuesta, ensure_ascii=False))

    except Exception as e:
        respuesta = {
            "ok": False,
            "error": str(e)
        }

        print(json.dumps(respuesta, ensure_ascii=False))
        sys.exit(1)

    finally:
        if temp_output_path and os.path.exists(temp_output_path):
            try:
                os.remove(temp_output_path)
            except Exception:
                pass
if __name__ == "__main__":
    main()
(base) ubuntu@ip-172-31-45-54:~/apps/hablar2026/hablarapp/backend/src/ml$ cat clasificar_fonetico.py
import os
import sys
import json
import numpy as np
import librosa
import tensorflow as tf
from scipy.signal import butter, lfilter

# ============================================================
# CLASIFICADOR FONÉTICO PARA HABLAR TSH
# Recibe:
#   1) ruta absoluta del audio
#   2) id_ejercicio
#
# Devuelve JSON por stdout para que Node.js lo pueda leer.
# ============================================================

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODELOS_DIR = os.path.join(BASE_DIR, "foneticos", "modelos")

SR = 16000
DURATION = 2.0
N_SAMPLES = int(SR * DURATION)

N_MELS = 64
HOP_LENGTH = 256
N_FFT = 1024

UMBRAL = 0.70

MODELOS_POR_EJERCICIO = {
    1: "ra.keras",
    2: "re.keras",
    3: "ri.keras",
    4: "ro.keras",
    5: "ru.keras",
    6: "rra.keras",
    7: "rre.keras",
    8: "rri.keras",
    9: "rro.keras",
    10: "rru.keras",
    11: "ferrocarril.keras",
    12: "cigarro.keras",
    13: "barril.keras",
    14: "rita.keras",
    15: "ruso.keras",
    16: "rama.keras",
    17: "rojo.keras",
    18: "rosa.keras",
}


def responder(payload, exit_code=0):
    print(json.dumps(payload, ensure_ascii=False))
    sys.exit(exit_code)


def filtro_pasa_banda(audio, lowcut=80, highcut=7500, sr=SR, order=5):
    nyq = 0.5 * sr
    low = lowcut / nyq
    high = highcut / nyq
    b, a = butter(order, [low, high], btype="band")
    return lfilter(b, a, audio)


def aplicar_vad(audio, sr=SR, top_db=20):
    intervalos = librosa.effects.split(audio, top_db=top_db)
    if len(intervalos) > 0:
        audio_voz = np.concatenate([audio[start:end] for start, end in intervalos])
        return audio_voz
    return audio


def cargar_y_procesar_audio(ruta_audio, sr=SR, n_samples=N_SAMPLES):
    try:
        audio, _ = librosa.load(ruta_audio, sr=sr, mono=True)
    except Exception as e:
        raise RuntimeError(f"No se pudo leer el audio: {str(e)}")

    audio = filtro_pasa_banda(audio, sr=sr)
    audio = aplicar_vad(audio, sr=sr)

    if np.max(np.abs(audio)) > 0:
        audio = audio / np.max(np.abs(audio))

    if len(audio) > n_samples:
        audio = audio[:n_samples]
    else:
        audio = np.pad(audio, (0, n_samples - len(audio)), mode="constant")

    return audio


def audio_a_mel(audio):
    mel = librosa.feature.melspectrogram(
        y=audio,
        sr=SR,
        n_fft=N_FFT,
        hop_length=HOP_LENGTH,
        n_mels=N_MELS
    )
    mel_db = librosa.power_to_db(mel, ref=np.max)
    mel_db = (mel_db - np.mean(mel_db)) / (np.std(mel_db) + 1e-8)
    return mel_db


def obtener_modelo_por_ejercicio(id_ejercicio):
    nombre_modelo = MODELOS_POR_EJERCICIO.get(id_ejercicio)

    if not nombre_modelo:
        raise RuntimeError(f"No existe modelo configurado para id_ejercicio={id_ejercicio}")

    ruta_modelo = os.path.join(MODELOS_DIR, nombre_modelo)

    if not os.path.exists(ruta_modelo):
        raise RuntimeError(f"No se encontró el modelo: {ruta_modelo}")

    return nombre_modelo, ruta_modelo


def calcular_porcentaje(prob_bien, umbral=UMBRAL):
    if prob_bien < umbral:
        calificacion = (prob_bien / umbral) * 50.0
    else:
        calificacion = 50.0 + ((prob_bien - umbral) / (1.0 - umbral)) * 50.0

    calificacion = max(0.0, min(100.0, calificacion))
    return calificacion


def clasificar_audio(ruta_audio, id_ejercicio):
    if not os.path.exists(ruta_audio):
        raise RuntimeError(f"El audio no existe: {ruta_audio}")

    nombre_modelo, ruta_modelo = obtener_modelo_por_ejercicio(id_ejercicio)

    modelo = tf.keras.models.load_model(ruta_modelo)

    audio = cargar_y_procesar_audio(ruta_audio)
    mel = audio_a_mel(audio)

    x_audio = mel[np.newaxis, ..., np.newaxis].astype(np.float32)

    pred = modelo.predict(x_audio, verbose=0)

    prob_bien = float(pred[0][0])
    porcentaje = calcular_porcentaje(prob_bien)

    estado = "bien" if porcentaje >= 50.0 else "mal"
    ejercicio_nombre = nombre_modelo.replace(".keras", "")
    clase = f"{ejercicio_nombre}_{estado}"

    return {
        "ok": True,
        "tipo": "fonetico",
        "archivo_entrada": ruta_audio,
        "id_ejercicio": id_ejercicio,
        "modelo_usado": nombre_modelo,
        "clase": clase,
        "estado": estado,
        "prob_bien": round(prob_bien, 6),
        "confianza_real": round(prob_bien * 100.0, 2),
        "porcentaje": round(porcentaje, 2)
    }


def main():
    try:
        if len(sys.argv) < 3:
            responder({
                "ok": False,
                "error": "Uso: python src/ml/clasificar_fonetico.py <ruta_audio> <id_ejercicio>"
            }, exit_code=1)

        ruta_audio = sys.argv[1]
        id_ejercicio = int(sys.argv[2])

        resultado = clasificar_audio(ruta_audio, id_ejercicio)
        responder(resultado, exit_code=0)

    except Exception as e:
        responder({
            "ok": False,
            "error": str(e)
        }, exit_code=1)


if __name__ == "__main__":
    main()
