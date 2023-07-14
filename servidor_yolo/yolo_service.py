import json
from flask import Flask, send_file, request, jsonify
import detect_modificado

app = Flask(__name__)
UPLOAD_FOLDER = "shared_pub_service/imagenes/inference/"

#Recibe el nombre de un fichero en el parámetro "filename". El fichero debe existir en la carpeta UPLOAD_FOLDER. Devuelve las monedas detectadas
@app.route('/detectfromfile', methods=['GET'])
def detect_from_file():
    data = request.get_json()
    nombre_fichero = data.get('filename')
    path_fichero = UPLOAD_FOLDER + nombre_fichero
    resultados = detect_modificado.detect_con_params(weights='runs/train/yolov7-custom12/weights/best.pt',
                                        source=path_fichero,
                                        img_size=640,
                                        conf_thres=0.25,
                                        view_img=False,
                                        save_txt=True,
                                        nosave=True)

    detect_modificado.print_resultados(resultados)

    response = {'success': True, 'resultado':resultados}
    return json.dumps(response)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
