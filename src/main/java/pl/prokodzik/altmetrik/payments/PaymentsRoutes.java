package pl.prokodzik.altmetrik.payments;

import static spark.Spark.get;

public class PaymentsRoutes {


    public static final String DEFAULT_PATH = "/payments";



    public static void main(String[] args) {

        get(DEFAULT_PATH + "/", (req, resp) -> {
            return
        });
    }


}
