import cv2
from PIL import Image


# https://www.datasmarts.net/como-cambiar-el-tamano-de-una-imagen-en-opencv/

def rescalar(image, width=None, height=None, inter=cv2.INTER_AREA):
    """
    Función que cambia el tamaño de una imagen preservando la relación de aspecto.
    :param image: Imagen a ser alterada.
    :param width: Ancho deseado.
    :param height: Altura deseada.
    :param inter: Método de interpolación (por defecto: cv2.INTER_AREA)
    :return: Imagen redimensionada.
    """
    # Extraemos las dimensiones originales.
    (original_height, original_width) = image.shape[:2]

    ratioH = 1
    ratioW = 1
    if original_height > height:
        ratioH = height / float(original_height)

    if float(original_width) > float(width):
        ratioW = width / float(original_width)

    # Como solo vamos a reducir, se utilizará el menor ratio que será el que implica mayor reducción
    if ratioW < ratioH:
        height = int(original_height * ratioW)
    else:
        width = int(original_width * ratioH)

    # Nuevo tamaño de la imagen
    new_size = (width, height)

    if ratioW == 1 and ratioH == 1:
        # La imagen es más pequeña o igual a las dimensiones requeridas
        imagenSalida = image
    else:
        # Usamos la función cv2.resize() para llevar a cabo el cambio de tamaño de la imagen; finalmente retornamos el
        # resultado.
        imagenSalida = cv2.resize(image, new_size, interpolation=inter)
    return imagenSalida


# nombreImagenOrigen nombre de la imagen origen incluyendo sin ruta
# nombreImagenRescalada nombre de la imagen escalada incluyendo sin ruta
def rescalar_img_from_file(nombreImagen):
    rutaOrigen = "imagenes/original/"
    rutaDestino = "imagenes/rescalada/"
    rutaRecursos = "imagenes/recursos/"
    rutaDefinitiva = "imagenes/inference/"
    nombreimagenFondo = "fondo.jpg"
    nombreImagenFondoRescalada = "fondoRescalado.jpg"

    pixelEscaladoW = 640
    pixelEscaladoH = 640

    imageFondo = cv2.imread(rutaRecursos + nombreimagenFondo)
    imagenFondoRescalada = cv2.resize(imageFondo, (pixelEscaladoW,pixelEscaladoH), interpolation=cv2.INTER_AREA)
    cv2.imwrite(rutaRecursos + nombreImagenFondoRescalada, imagenFondoRescalada)

    image = cv2.imread(rutaOrigen + nombreImagen)
    imagenRescalada = rescalar(image, width=pixelEscaladoW, height=pixelEscaladoH)
    cv2.imwrite(rutaDestino + nombreImagen, imagenRescalada)

    imagenDefinitiva = Image.open(rutaRecursos + nombreImagenFondoRescalada)
    imagenSuperponer = Image.open(rutaDestino + nombreImagen)
    imagenDefinitiva.paste(imagenSuperponer, (0,0))
    imagenDefinitiva.save(rutaDefinitiva + nombreImagen)
