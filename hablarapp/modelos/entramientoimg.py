# ============================================================
# CLASIFICACIÓN DE IMÁGENES OROFACIALES CON CNN
# ============================================================
# Este script entrena una red neuronal convolucional para clasificar
# imágenes orofaciales almacenadas en Google Drive.

import os
import json
import numpy as np
import matplotlib.pyplot as plt
import tensorflow as tf

from tensorflow import keras
from tensorflow.keras import layers
from tensorflow.keras.callbacks import EarlyStopping, ModelCheckpoint, ReduceLROnPlateau


# ============================================================
# 1. CONEXIÓN CON GOOGLE DRIVE
# ============================================================
# Se monta Google Drive para acceder al dataset y guardar el modelo entrenado.

from google.colab import drive
drive.mount('/content/drive')

print(os.path.exists("/content/drive"))
print(os.path.exists("/content/drive/MyDrive"))
print(os.listdir("/content/drive/MyDrive")[:100])


# ============================================================
# 2. RUTAS Y PARÁMETROS GENERALES
# ============================================================
# Se definen las rutas del dataset y los parámetros principales del entrenamiento.

DATASET_DIR = "/content/drive/MyDrive/DSHABLAR"

TRAIN_DIR = os.path.join(DATASET_DIR, "train")
VALID_DIR = os.path.join(DATASET_DIR, "valid")
TEST_DIR  = os.path.join(DATASET_DIR, "test")

IMG_HEIGHT = 224
IMG_WIDTH = 224
BATCH_SIZE = 32
EPOCHS = 30
SEED = 42

MODEL_SAVE_PATH = "/content/drive/MyDrive/modelo_orofaciales.keras"
CLASS_NAMES_PATH = "/content/drive/MyDrive/class_names.json"

print("TensorFlow:", tf.__version__)
print("Train:", TRAIN_DIR)
print("Valid:", VALID_DIR)
print("Test :", TEST_DIR)


# ============================================================
# 3. CARGA DEL DATASET
# ============================================================
# Se cargan las imágenes desde carpetas separadas para entrenamiento,
# validación y prueba. Las etiquetas se generan automáticamente.

train_ds = keras.utils.image_dataset_from_directory(
    TRAIN_DIR,
    labels="inferred",
    label_mode="categorical",
    batch_size=BATCH_SIZE,
    image_size=(IMG_HEIGHT, IMG_WIDTH),
    shuffle=True,
    seed=SEED
)

valid_ds = keras.utils.image_dataset_from_directory(
    VALID_DIR,
    labels="inferred",
    label_mode="categorical",
    batch_size=BATCH_SIZE,
    image_size=(IMG_HEIGHT, IMG_WIDTH),
    shuffle=False
)

test_ds = keras.utils.image_dataset_from_directory(
    TEST_DIR,
    labels="inferred",
    label_mode="categorical",
    batch_size=BATCH_SIZE,
    image_size=(IMG_HEIGHT, IMG_WIDTH),
    shuffle=False
)


# ============================================================
# 4. DETECCIÓN Y GUARDADO DE CLASES
# ============================================================
# Se obtienen los nombres de las clases y se guardan en un archivo JSON
# para usarlos posteriormente durante la predicción.

class_names = train_ds.class_names
num_classes = len(class_names)

print("Clases detectadas:")
for i, name in enumerate(class_names):
    print(f"{i}: {name}")

with open(CLASS_NAMES_PATH, "w", encoding="utf-8") as f:
    json.dump(class_names, f, ensure_ascii=False, indent=4)

print("\nArchivo de clases guardado en:", CLASS_NAMES_PATH)


# ============================================================
# 5. VISUALIZACIÓN DE IMÁGENES DEL DATASET
# ============================================================
# Se muestran algunas imágenes de entrenamiento con su clase correspondiente
# para verificar que el dataset se haya cargado correctamente.

plt.figure(figsize=(10, 10))

for images, labels in train_ds.take(1):
    for i in range(min(9, len(images))):
        ax = plt.subplot(3, 3, i + 1)
        plt.imshow(images[i].numpy().astype("uint8"))
        class_idx = np.argmax(labels[i].numpy())
        plt.title(class_names[class_idx])
        plt.axis("off")

plt.tight_layout()
plt.show()


# ============================================================
# 6. AUMENTO DE DATOS
# ============================================================
# Se aplican transformaciones aleatorias para mejorar la generalización
# del modelo y reducir el sobreajuste.

data_augmentation = keras.Sequential([
    layers.RandomFlip("horizontal"),
    layers.RandomRotation(0.08),
    layers.RandomZoom(0.10),
    layers.RandomContrast(0.10),
], name="data_augmentation")


# ============================================================
# 7. DEFINICIÓN DEL MODELO CNN
# ============================================================
# Se construye una red neuronal convolucional para extraer características
# visuales de las imágenes y clasificarlas en las clases detectadas.

model = keras.Sequential([
    layers.Input(shape=(IMG_HEIGHT, IMG_WIDTH, 3)),

    data_augmentation,
    layers.Rescaling(1./255),

    layers.Conv2D(32, 3, activation="relu", padding="same"),
    layers.MaxPooling2D(),

    layers.Conv2D(64, 3, activation="relu", padding="same"),
    layers.MaxPooling2D(),

    layers.Conv2D(128, 3, activation="relu", padding="same"),
    layers.MaxPooling2D(),

    layers.Conv2D(256, 3, activation="relu", padding="same"),
    layers.MaxPooling2D(),

    layers.Dropout(0.3),
    layers.Flatten(),
    layers.Dense(256, activation="relu"),
    layers.Dropout(0.4),
    layers.Dense(num_classes, activation="softmax")
])


# ============================================================
# 8. COMPILACIÓN DEL MODELO
# ============================================================
# Se define el optimizador, la función de pérdida y la métrica de evaluación.

model.compile(
    optimizer=keras.optimizers.Adam(learning_rate=1e-4),
    loss="categorical_crossentropy",
    metrics=["accuracy"]
)

model.summary()


# ============================================================
# 9. CALLBACKS DE ENTRENAMIENTO
# ============================================================
# Se utilizan callbacks para detener el entrenamiento si no mejora,
# guardar el mejor modelo y ajustar la tasa de aprendizaje.

callbacks = [
    EarlyStopping(
        monitor="val_loss",
        patience=6,
        restore_best_weights=True,
        verbose=1
    ),
    ModelCheckpoint(
        filepath=MODEL_SAVE_PATH,
        monitor="val_accuracy",
        save_best_only=True,
        verbose=1
    ),
    ReduceLROnPlateau(
        monitor="val_loss",
        factor=0.5,
        patience=3,
        verbose=1
    )
]


# ============================================================
# 10. ENTRENAMIENTO DEL MODELO
# ============================================================
# Se entrena la CNN usando el conjunto de entrenamiento y validación.

history = model.fit(
    train_ds,
    validation_data=valid_ds,
    epochs=EPOCHS,
    callbacks=callbacks
)


# ============================================================
# 11. VERIFICACIÓN DEL MODELO GUARDADO
# ============================================================
# Se comprueba que el archivo del modelo haya sido creado correctamente.

print("Ruta del modelo:", MODEL_SAVE_PATH)
print("¿Existe el archivo?:", os.path.exists(MODEL_SAVE_PATH))


# ============================================================
# 12. GRÁFICAS DE ENTRENAMIENTO
# ============================================================
# Se grafican la precisión y la pérdida durante el entrenamiento
# para analizar el comportamiento del modelo.

acc = history.history["accuracy"]
val_acc = history.history["val_accuracy"]
loss = history.history["loss"]
val_loss = history.history["val_loss"]

plt.figure(figsize=(14, 5))

plt.subplot(1, 2, 1)
plt.plot(acc, label="Train Accuracy")
plt.plot(val_acc, label="Valid Accuracy")
plt.title("Precisión")
plt.xlabel("Épocas")
plt.ylabel("Accuracy")
plt.legend()

plt.subplot(1, 2, 2)
plt.plot(loss, label="Train Loss")
plt.plot(val_loss, label="Valid Loss")
plt.title("Pérdida")
plt.xlabel("Épocas")
plt.ylabel("Loss")
plt.legend()

plt.tight_layout()
plt.show()


# ============================================================
# 13. EVALUACIÓN FINAL EN EL CONJUNTO DE PRUEBA
# ============================================================
# Se carga el mejor modelo guardado y se evalúa con imágenes no vistas.

best_model = keras.models.load_model(MODEL_SAVE_PATH)

test_loss, test_acc = best_model.evaluate(test_ds, verbose=1)

print(f"\nAccuracy en test: {test_acc:.4f}")
print(f"Loss en test:     {test_loss:.4f}")


# ============================================================
# 14. REPORTE DE CLASIFICACIÓN
# ============================================================
# Se generan predicciones sobre el conjunto de prueba para obtener
# métricas por clase como precisión, recall y F1-score.

from sklearn.metrics import classification_report, confusion_matrix
import seaborn as sns

y_true = []
y_pred = []

for images, labels in test_ds:
    preds = best_model.predict(images, verbose=0)
    y_true.extend(np.argmax(labels.numpy(), axis=1))
    y_pred.extend(np.argmax(preds, axis=1))

print(classification_report(y_true, y_pred, target_names=class_names, digits=4))
