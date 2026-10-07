# Bundle

Эта папка содержит бинарные зависимости, упаковываемые в релиз.
Сами бинарники **не хранятся в Git** — их нужно скачать отдельно.

## Что должно лежать в этой папке

| Файл | Назначение | Источник |
|---|---|---|
| `yt-dlp.exe` | Скачивание видео и аудио | https://github.com/yt-dlp/yt-dlp/releases |
| `ffmpeg.exe` | Конвертация аудио, встраивание метаданных | https://www.gyan.dev/ffmpeg/builds/ (essentials build) |
| `ffprobe.exe` | Анализ медиафайлов (используется ffmpeg) | оттуда же |
| `deno.exe` | JS-рантайм для решения задач YouTube | https://github.com/denoland/deno/releases |

## Как получить

1. Скачай `yt-dlp.exe` со страницы релизов yt-dlp.
2. Скачай **essentials build** FFmpeg, распакуй архив, возьми `ffmpeg.exe` и `ffprobe.exe` из папки `bin`.
3. Скачай `deno.exe` (Windows-версия) со страницы релизов Deno.
4. Положи все 4 файла в эту папку.