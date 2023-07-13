import os
from flask import Flask, send_file, jsonify, request
import requests
from uuid import uuid4
import json
import rescalarimg

app = Flask(__name__)
UPLOAD_FOLDER = "imagenes/original"

# @app.route('/detectimg', methods=['GET'])
# def detectar():
#     url = 'http://yolov7serv:5000/testdetect'
#     headers = {'Content-Type': 'application/json'}
#     try:
#         response = requests.get(url, headers=headers)
#         response.raise_for_status()
#         resultados = response.json()
#         return jsonify(resultados)
#     except requests.exceptions.RequestException as e:
#         print('Error al llamar al servicio "detecciones":', e)
#         return jsonify({'success': False, 'error': str(e)})  # Devuelve una respuesta de error en formato JSON


# Recibe una imagen en un atributo de nombre "file" y devuelve las monedas detectadas
@app.route('/detect_coins', methods=['POST'])
def detect_coins():
    if 'file' not in request.files:
        return 'No se ha recibido el archivo', 400
    file = request.files['file']
    if file.filename == '':
        return 'No se seleccionó correctamente el archivo', 400

    # nombre único con la extensión del nombre original
    nombre_unico = str(uuid4()) + os.path.splitext(file.filename)[1]
    ruta_archivo = os.path.join(UPLOAD_FOLDER, nombre_unico)  # Ruta donde se guardará el archivo
    file.save(ruta_archivo)

    #rescalar
    rescalarimg.rescalar_img_from_file(nombre_unico)

    url = 'http://yolov7serv:5000/detectfromfile'
    headers = {'Content-Type': 'application/json'}
    data = json.dumps({'filename': nombre_unico})
    try:
        response = requests.get(url, headers=headers, data=data)
        response.raise_for_status()
        resultados = response.json()
        return jsonify(resultados)
    except requests.exceptions.RequestException as e:
        print('Error al llamar al servicio "detecciones":', e)
        return jsonify({'success': False, 'error': str(e)})  # Devuelve una respuesta de error en formato JSON


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001)
