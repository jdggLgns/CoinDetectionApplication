from SqlUtils import get_conection


class GestionUsuarios:
    @staticmethod
    def register(_name, _mail, _userid, _password):
        con = None
        all_success = True
        try:
            if _userid and _password:
                con = get_conection()
                cur = con.cursor()
                cur.execute("INSERT INTO usuarios (name, mail, userid, password) VALUES (%s, %s, %s, %s)",
                            (_name, _mail, _userid, _password))
                con.commit()
            else:
                all_success = False
        except BaseException as e:
            all_success = False
        finally:
            if con:
                con.close()
            return all_success

    @staticmethod
    def login(userid, password):
        con = None
        response = {'success': False}
        try:
            con = get_conection()
            cur = con.cursor()
            cur.execute("SELECT name, mail, userid, password FROM usuarios WHERE userid = %s AND password = %s",
                        (userid, password))
            result = cur.fetchone()

            if result:
                name, mail, db_userid, db_password = result

                response = {
                    'success': True,
                    'name': name,
                    'mail': mail,
                    'userid': db_userid,
                    'password': db_password
                }
            else:
                response = {'success': False}
        except BaseException as e:
            response = {'success': False}
        finally:
            if con:
                con.close()
            return response
