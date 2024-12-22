package org.wolrus.digital_system.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

import static java.util.Collections.singletonList;

@Builder
public record TelegramRequest(
        @JsonProperty("chat_id")
        String chatId,
        String text,
        @JsonProperty("reply_markup")
        ReplyMarkup replyMarkup
) {

    @Builder
    public record ReplyMarkup(
            @JsonProperty("inline_keyboard")
            List<List<InlineKeyboardButton>> inlineKeyboard
    ) {}

    @Builder
    public record InlineKeyboardButton(
            String text,
            String callback_data
    ) {}

    public static TelegramRequest feedbackKeyboard(Integer id, String chatId, String text) {
        return TelegramRequest.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(ReplyMarkup.builder()
                        .inlineKeyboard(singletonList(List.of(
                                InlineKeyboardButton.builder()
                                        .text("Да")
                                        .callback_data(String.format("%s_%s_request_yes", id, chatId))
                                        .build(),
                                InlineKeyboardButton.builder()
                                        .text("Нет")
                                        .callback_data(String.format("%s_%s_request_no", id, chatId))
                                        .build()
                        )))
                        .build())
                .build();
    }

}
