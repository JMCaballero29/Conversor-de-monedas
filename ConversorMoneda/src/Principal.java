import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Scanner;

public class Principal {
    private static final String API_URL = "Coloca tu clave de ExchangeRate API aquí";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean continuar = true;

        while (continuar) {
            double cantidad = solicitaCantidad(scanner);
            int seleccion = solicitaMoneda(scanner);
            conversion(cantidad, seleccion);

            System.out.println("¿Deseas realizar otra conversión? (s/n)");
            String respuesta = scanner.next();

            if (!respuesta.equalsIgnoreCase("s")) {
                continuar = false;
            }
        }

        scanner.close();
    }

    public static double solicitaCantidad(Scanner scanner) {
        double cantidad = 0.0;
        while (cantidad <= 0.0) {
            try {
                System.out.println("Ingrese la cantidad a convertir:");
                cantidad = scanner.nextDouble();
                if (cantidad <= 0.0) {
                    System.out.println("Ingrese un valor válido mayor que cero.");
                }
            } catch (Exception e) {
                System.out.println("Error: Ingrese solo números.");
                scanner.nextLine();
            }
        }
        return cantidad;
    }

    public static int solicitaMoneda(Scanner scanner) {
        int seleccion = 0;
        while (seleccion < 1 || seleccion > 4) {
            try {
                System.out.println("*** Selecciona el tipo de moneda que quieres convertir: ***");
                System.out.println("1.) Dolar -> Peso Mexicano");
                System.out.println("2.) Peso Mexicano -> Dolar");
                System.out.println("3.) Dolar -> Euros");
                System.out.println("4.) Euros -> Dolar");
                seleccion = scanner.nextInt();
                if (seleccion < 1 || seleccion > 4) {
                    System.out.println("Seleccione una opción válida (1-4).");
                }
            } catch (Exception e) {
                System.out.println("Error: Ingrese solo números.");
                scanner.nextLine();
            }
        }
        return seleccion;
    }

    public static void conversion(double cantidad, int seleccion) {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .build();
            HttpResponse<String> response = client
                    .send(request, HttpResponse.BodyHandlers.ofString());

            String json = response.body();
            TipoMoneda tipoMoneda = gson.fromJson(json, TipoMoneda.class);

            if (tipoMoneda != null && tipoMoneda.getConversionRates() != null) {
                Map<String, Double> conversionRates = tipoMoneda.getConversionRates();
                double rate = 0.0;
                String monedaDestino = "";

                if (seleccion == 1) {
                    rate = conversionRates.get("MXN");
                    monedaDestino = "Peso Mexicano";
                } else if (seleccion == 2) {
                    rate = 1 / conversionRates.get("MXN");
                    monedaDestino = "Dólares";
                } else if (seleccion == 3) {
                    rate = conversionRates.get("EUR");
                    monedaDestino = "Euro";
                } else if (seleccion == 4) {
                    rate = 1 / conversionRates.get("EUR");
                    monedaDestino = "Dólares";
                }

                double convertedAmount = cantidad * rate;
                System.out.printf("%.2f USD equivale a %.2f en %s.\n", cantidad, convertedAmount, monedaDestino);

            }else {
                    System.out.println("La respuesta del API no tiene el formato esperado.");
            }
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
}
