package org.example.model;

public enum ErrorType {
    INVALID_URL("Неверная ссылка. Проверьте, что это правильный URL."),
    GEO_BLOCKED("Видео недоступно в вашем регионе. Попробуйте использовать VPN или прокси."),
    PRIVATE_VIDEO("Видео приватное. Для доступа нужна авторизация (cookies)."),
    RATE_LIMITED("Слишком много запросов к YouTube. Попробуйте позже."),
    NETWORK_ERROR("Обрыв сети или проблема с соединением. Проверьте интернет."),
    BOT_CHECK("YouTube требует подтверждение, что вы не робот. Используйте cookies из браузера."),
    VIDEO_UNAVAILABLE("Видео недоступно или удалено автором."),
    PLAYLIST_NOT_FOUND("Плейлист не существует или был удалён."),
    SIGN_IN_REQUIRED("Требуется вход в аккаунт YouTube для доступа к этому контенту."),
    UNKNOWN("Неизвестная ошибка. Смотрите подробности в логах.");

    private final String userMessage;

    ErrorType(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }
}