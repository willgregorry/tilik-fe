@echo off
echo ========================================================
echo  Tilik Android - Reverse Port Forwarding (localhost:8000)
echo ========================================================
echo.
echo [*] Memeriksa koneksi perangkat ADB...
adb devices
echo.
echo [*] Mengarahkan localhost:8000 di HP ke backend di PC...
adb reverse tcp:8000 tcp:8000
if %ERRORLEVEL% EQU 0 (
    echo.
    echo [OK] BERHASIL! HP Android kini terhubung langsung ke http://localhost:8000/
) else (
    echo.
    echo [ERROR] Gagal menjalankan adb reverse. Pastikan HP terhubung via USB dan USB Debugging aktif.
)
echo.
pause
