import java.io.*;
import java.net.*;
import java.util.*;

public class CalculatorHttpServer {
    private static final String STUDENT_NAME = "Васильев Андрей Алексеевич"; /* ФИО и шифр */
    private static final String STUDENT_CODE = "23И0668";
    private static final int STUDENT_NUMBER = 1;
    private static final int PORT = Integer.parseInt(STUDENT_NUMBER + "" + STUDENT_NUMBER);

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("HTTP Server with Calculator");
        System.out.println("Student: " + STUDENT_NAME);
        System.out.println("Code: " + STUDENT_CODE);
        System.out.println("Port: " + PORT);
        System.out.println("==========================================");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Calculator HTTP Server запущен на порту " + PORT);
            System.out.println("Доступен по адресу: http://localhost:" + PORT);
            System.out.println("Для вычислений используйте: http://localhost:" + PORT + "/calculate?a=5&b=3&op=+");
            System.out.println("Для просмотра информации: http://localhost:" + PORT + "/info");

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleClient(clientSocket);
                } catch (IOException e) {
                    System.err.println("Ошибка при обработке клиента: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка запуска сервера: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String requestLine = in.readLine();
        if (requestLine == null) return;

        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 2) return;

        String method = requestParts[0];
        String pathWithParams = requestParts[1];

        String[] pathParts = pathWithParams.split("\\?");
        String path = pathParts[0];
        Map<String, String> params = new HashMap<>();

        if (pathParts.length > 1) {
            String query = pathParts[1];
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }

        if (method.equals("GET")) {
            if (path.equals("/calculate")) {
                handleCalculate(params, out);
            } else if (path.equals("/info")) {
                handleInfo(out);
            } else if (path.equals("/")) {
                handleHome(out);
            } else {
                handleNotFound(out);
            }
        } else {
            handleNotFound(out);
        }

        out.flush();
    }

    private static void handleHome(PrintWriter out) {
        String html = "<html><head><title>Калькулятор HTTP-сервер</title>" +
                "<style>body { font-family: Arial, sans-serif; margin: 40px; } " +
                ".container { max-width: 800px; margin: 0 auto; } " +
                ".example { background: #f5f5f5; padding: 15px; border-radius: 5px; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<h1>Добро пожаловать на HTTP-сервер с калькулятором!</h1>" +
                "<h2>Студент: " + STUDENT_NAME + "</h2>" +
                "<h3>Шифр: " + STUDENT_CODE + "</h3>" +
                "<h3>Порт сервера: " + PORT + "</h3>" +
                "<hr>" +
                "<h3>Доступные возможности:</h3>" +
                "<div class='example'>" +
                "<h4>Калькулятор:</h4>" +
                "<p>GET /calculate?a={число}&b={число}&op={операция}</p>" +
                "<p>Поддерживаемые операции: + (сложение), - (вычитание), * (умножение), / (деление)</p>" +
                "<p><strong>Примеры:</strong></p>" +
                "<ul>" +
                "<li><a href='/calculate?a=5&b=3&op=+'>/calculate?a=5&b=3&op=+</a></li>" +
                "<li><a href='/calculate?a=10&b=2&op=*'>/calculate?a=10&b=2&op=*</a></li>" +
                "<li><a href='/calculate?a=15&b=3&op=/'>/calculate?a=15&b=3&op=/</a></li>" +
                "</ul>" +
                "</div>" +
                "<br>" +
                "<p><a href='/info'>Информация о сервере</a></p>" +
                "</div></body></html>";

        sendHttpResponse(out, 200, html);
    }

    private static void handleInfo(PrintWriter out) {
        String html = "<html><head><title>Информация о сервере</title></head><body>" +
                "<h1>Информация о HTTP-сервере с калькулятором</h1>" +
                "<h2>Данные студента:</h2>" +
                "<ul>" +
                "<li><strong>ФИО:</strong> " + STUDENT_NAME + "</li>" +
                "<li><strong>Шифр:</strong> " + STUDENT_CODE + "</li>" +
                "<li><strong>Порядковый номер:</strong> " + STUDENT_NUMBER + "</li>" +
                "<li><strong>Порт сервера:</strong> " + PORT + "</li>" +
                "</ul>" +
                "<h2>Техническая информация:</h2>" +
                "<ul>" +
                "<li><strong>Язык программирования:</strong> Java</li>" +
                "<li><strong>Протокол:</strong> HTTP/1.1</li>" +
                "<li><strong>Тип сервера:</strong> Однопоточный HTTP-сервер</li>" +
                "<li><strong>Реализованные функции:</strong> Арифметические операции (+, -, *, /)</li>" +
                "</ul>" +
                "<br>" +
                "<a href='/'>Вернуться на главную</a>" +
                "</body></html>";

        sendHttpResponse(out, 200, html);
    }

    private static void handleCalculate(Map<String, String> params, PrintWriter out) {
        try {
            double a = Double.parseDouble(params.get("a"));
            double b = Double.parseDouble(params.get("b"));
            String op = params.get("op");

            double result;
            String operationName;

            switch (op) {
                case "+":
                    result = a + b;
                    operationName = "сложение";
                    break;
                case "-":
                    result = a - b;
                    operationName = "вычитание";
                    break;
                case "*":
                    result = a * b;
                    operationName = "умножение";
                    break;
                case "/":
                    if (b == 0) {
                        sendHttpResponse(out, 400,
                                "<html><body><h1>Ошибка 400: Деление на ноль</h1>" +
                                        "<p>Нельзя делить на ноль!</p>" +
                                        "<a href='/'>Вернуться на главную</a></body></html>");
                        return;
                    }
                    result = a / b;
                    operationName = "деление";
                    break;
                default:
                    sendHttpResponse(out, 400,
                            "<html><body><h1>Ошибка 400: Неподдерживаемая операция</h1>" +
                                    "<p>Поддерживаемые операции: +, -, *, /</p>" +
                                    "<a href='/'>Вернуться на главную</a></body></html>");
                    return;
            }

            String html = "<html><head><title>Результат вычисления</title></head><body>" +
                    "<h1>Результат вычисления</h1>" +
                    "<h2>" + a + " " + op + " " + b + " = " + result + "</h2>" +
                    "<p><strong>Операция:</strong> " + operationName + "</p>" +
                    "<p><strong>Первое число:</strong> " + a + "</p>" +
                    "<p><strong>Второе число:</strong> " + b + "</p>" +
                    "<p><strong>Результат:</strong> " + result + "</p>" +
                    "<br>" +
                    "<a href='/'>Вернуться на главную</a>" +
                    "</body></html>";

            sendHttpResponse(out, 200, html);

        } catch (NumberFormatException e) {
            sendHttpResponse(out, 400,
                    "<html><body><h1>Ошибка 400: Некорректные числа</h1>" +
                            "<p>Параметры 'a' и 'b' должны быть числами!</p>" +
                            "<a href='/'>Вернуться на главную</a></body></html>");
        } catch (NullPointerException e) {
            sendHttpResponse(out, 400,
                    "<html><body><h1>Ошибка 400: Отсутствуют параметры</h1>" +
                            "<p>Необходимые параметры: a, b, op</p>" +
                            "<p>Пример: /calculate?a=5&b=3&op=+</p>" +
                            "<a href='/'>Вернуться на главную</a></body></html>");
        }
    }

    private static void handleNotFound(PrintWriter out) {
        String html = "<html><body><h1>404 Not Found</h1>" +
                "<p>Запрошенная страница не найдена</p>" +
                "<a href='/'>Вернуться на главную</a></body></html>";
        sendHttpResponse(out, 404, html);
    }

    private static void sendHttpResponse(PrintWriter out, int statusCode, String body) {
        out.println("HTTP/1.1 " + statusCode + " " + getStatusText(statusCode));
        out.println("Content-Type: text/html; charset=UTF-8");
        out.println("Content-Length: " + body.getBytes().length);
        out.println("Connection: close");
        out.println();
        out.println(body);
    }

    private static String getStatusText(int statusCode) {
        switch (statusCode) {
            case 200: return "OK";
            case 400: return "Bad Request";
            case 404: return "Not Found";
            default: return "Unknown";
        }
    }
}