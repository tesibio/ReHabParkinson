# Rehabilitación Párkinson

Cliente de aplicación del proyecto "Desarrollo de una aplicacón móvil para el análisis de movimientos de los pacientes con la enfermedad de Párkinson mediante el Uso de Inteligencia Artificial".

## Rquerimientos

- [JDK 21](https://www.oracle.com/mx/java/technologies/downloads/#java21)
- [OpenCV 4.11.0 SDK](https://github.com/opencv/opencv/releases/tag/4.11.0)

## Instalación de OpenCV

Antes de poder armar el proyecto es necesario instalar el SDK de OpenCV. Este está disponible [aquí](https://github.com/opencv/opencv/releases/tag/4.11.0).

1. Descargar `opencv-4.11.0-android-sdk.zip`.
2. Descomprimir (1.3GB).
3. Copiar todos los archivos del SDK descomprimido a la carpeta `opencv-4.11.0-android-sdk` **SIN** reemplazar aquellos ya existentes.

Desde la pestaña de `Proyecto` (vista Android), debería haber tres carpetas:
- app
- opencv
- Gradle Scripts

En caso de que Android Studio no encuentre opencv, puede [importarse nuevamente](https://docs.opencv.org/4.x/d5/df8/tutorial_dev_with_OCV_on_Android.html).