package org.y42i3Z.tg_journal_bot;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static org.y42i3Z.tg_journal_bot.Config.BOT_NAME;
import static org.y42i3Z.tg_journal_bot.Config.TOKEN;
import static org.y42i3Z.tg_journal_bot.GitUtils.*;

@Slf4j
@Component
@AllArgsConstructor
public class JournalingBot extends TelegramLongPollingBot {

    DisposableGitWrapper wrapper;

    @SneakyThrows
    @Override
    public void onUpdatesReceived(List<Update> updates) {
        log.info("Got {} updates", updates.size());
        String branchName = getBranchName();
        checkoutBranch(wrapper.getGit(), branchName);

        updates.forEach(this::onUpdateReceived);

        pushCommittedEntries(wrapper.getGit(), updates, branchName);
        sendSummaryMessage(updates);
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message msg = update.getMessage();
            commitMessage(wrapper.getGit(), msg.getText(), msg.getDate());
        }
    }

    void sendSummaryMessage(List<Update> updates) {
        updates.stream()
                .filter(update -> update.hasMessage() && update.getMessage().hasText())
                .map(update -> Map.entry(update.getMessage().getChatId(), update))
                .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())))
                .forEach((chatId, chatUpdates) -> {
                    String chatSummary = chatUpdates.stream()
                            .map(update -> {
                                String text = update.getMessage().getText();
                                return text.substring(0, Math.min(text.length(), 50));
                            })
                            .collect(Collectors.joining("\n"));

                    SendMessage message = new SendMessage()
                            .setChatId(chatId)
                            .setText(String.format("%d messages were processed:\n%s", chatUpdates.size(), chatSummary));
                    try {
                        execute(message);
                        log.info("Summary message has been sent");
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return TOKEN;
    }

}