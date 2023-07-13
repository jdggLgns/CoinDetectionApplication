import pymysql as pymysql
from Project_properties import conexiones


def get_conection():
    con = None
    try:
        con = pymysql.connect(host=conexiones["conn_interna"], user='root', password='coin3!Detection',
                              db='coindetection_database')
    except BaseException as e:
        con = None
    return con


# Devuelve el id del regidtro creado si se ha insertado correctamente y None en caso contrario
def exec_insert(sql, params):
    con = None
    id_prod = None
    cur = None
    try:
        con = get_conection()
        cur = con.cursor()
        cur.execute(sql, params)
        id_prod = cur.lastrowid
        con.commit()
    except BaseException as e:
        print('Error al llamar al método "exec_insert":', e)
        id_prod = None
    finally:
        if cur:
            cur.close()
        if con:
            con.close()
        print('SqlUtils.exec_insert - id_prod:' + str(id_prod))
        return id_prod


def exec_delete(sql, params):
    con = None
    cur = None
    all_success = False
    try:
        con = get_conection()
        cur = con.cursor()
        cur.execute(sql, params)
        con.commit()
        if cur.rowcount > 0:
            all_success = True
    except BaseException as e:
        all_success = None
    finally:
        if cur:
            cur.close()
        if con:
            con.close()
        return all_success


def exec_update(sql, params):
    con = None
    cur = None
    all_success = False
    try:
        con = get_conection()
        cur = con.cursor()
        cur.execute(sql, params)
        con.commit()
        if cur.rowcount > 0:
            all_success = True
    except BaseException as e:
        all_success = None
    finally:
        if cur:
            cur.close()
        if con:
            con.close()
        return all_success


def exec_select(sql, params):
    con = None
    cur = None
    rows = None
    try:
        con = get_conection()
        cur = con.cursor()
        cur.execute(sql, params)
        rows = cur.fetchall()
    except BaseException as e:
        rows = None
    finally:
        if cur:
            cur.close()
        if con:
            con.close()
        return rows
