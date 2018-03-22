package owl.app.limpia_publica.api;

/**
 * Created by giusseppe on 07/03/2018.
 */

public class Api {

    //private static final String ROOT_URL = "http://cosamaloapan.mx/pagos/vistas/Json/";
    private static final String ROOT_URL = "http://192.168.1.68/pagos/vistas/Json/";
    //private static final String ROOT_URL = "http://192.168.1.77/LimpiaPublicaJson/";
    private static final String ROOT_LOGIN = ROOT_URL + "Api.php?apicall=";

    //login
    public static final String URL_LOGIN = ROOT_LOGIN + "login";

    //cobros
    private static final String ROOT_COBROS = ROOT_URL + "cobro/Api/Api.php?apicall=";
    public static final String URL_CREATE_PAGO_DOMICILIO = ROOT_COBROS + "createpagodomicilio";
    public static final String URL_CREATE_PAGO_COMERCIO = ROOT_COBROS + "createpago";
    public static final String URL_CREATE_PAGO_COMERCIO_FOTO = ROOT_URL + "detalle/upload.php";

    //Codigos de Request Handler
    public static final int CODE_GET_REQUEST = 1024;
    public static final int CODE_POST_REQUEST = 1025;
    public static final int PICK_IMAGE_REQUEST = 1;
}
