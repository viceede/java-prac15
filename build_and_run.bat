```batch
@echo off
echo ==========================================
echo Компиляция и запуск HTTP-сервера с калькулятором
echo ==========================================

echo Компиляция CalculatorHttpServer.java...
javac CalculatorHttpServer.java

if %errorlevel% neq 0 (
    echo Ошибка компиляции!
    pause
    exit /b %errorlevel%
)

echo Компиляция успешно завершена!
echo.
echo Запуск сервера...
echo ==========================================
echo Сервер будет доступен по адресу: http://localhost:11
echo ==========================================
echo.

java CalculatorHttpServer

pause