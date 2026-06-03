# ============================================================
# 1. PROCESAMIENTO AVANZADO: FILTRADO Y VAD
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
