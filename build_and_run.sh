#!/bin/bash
echo "=========================================="
echo "Компиляция и запуск HTTP-сервера с калькулятором"
echo "=========================================="

echo "Компиляция CalculatorHttpServer.java..."
javac CalculatorHttpServer.java

if [ $? -ne 0 ]; then
    echo "Ошибка компиляции!"
    exit 1
fi

echo "Компиляция успешно завершена!"
echo ""
echo "Запуск сервера..."
echo "=========================================="
echo "Сервер будет доступен по адресу: http://localhost:11"
echo "=========================================="
echo ""

java CalculatorHttpServer