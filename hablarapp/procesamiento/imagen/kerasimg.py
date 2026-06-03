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
