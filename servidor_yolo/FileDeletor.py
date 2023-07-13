import os

# Obtener la ruta del directorio a ordenar
directorio = "/home/dani/YOLO_V7/imagenes/TodasLasImagenesEtiquetadas/"

# Ordenar los elementos del directorio por nombre
elementos = sorted(os.listdir(directorio))

# Recorrer los elementos del directorio
for elemento in elementos:
    # Comprobar si el elemento es una imagen
    if elemento.endswith(".txt"):
        # Obtener el nombre del archivo sin la extensión
        nombre_archivo = os.path.splitext(elemento)[0]

        # Comprobar si el archivo de texto existe para este nombre de archivo
        nombre_imagen = f"{nombre_archivo}.jpg"
        if nombre_imagen in elementos:
            print(f"El archivo de texto {nombre_imagen} existe para la imagen {elemento}")
        else:
            print(f"El archivo de texto {nombre_imagen} NO existe para la imagen {elemento}, se eliminará la imagen")
            # Eliminar la imagen
            os.remove(os.path.join(directorio, elemento))
